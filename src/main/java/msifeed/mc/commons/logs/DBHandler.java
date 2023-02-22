package msifeed.mc.commons.logs;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import msifeed.mc.sys.config.ConfigBuilder;
import msifeed.mc.sys.config.ConfigEvent;
import msifeed.mc.sys.config.JsonConfig;
import msifeed.mc.sys.config.adapters.ZoneIdAdapter;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DBHandler {
    private final Logger LOGGER = LogManager.getLogger("More.Logs");
    private JsonConfig<ConfigSection> config = ConfigBuilder.of(ConfigSection.class, "database.json")
            .addAdapter(ZoneId.class, new ZoneIdAdapter())
            .create();

    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private HikariDataSource dataSource;

    @SubscribeEvent
    public void onReloadDone(ConfigEvent.AfterUpdate event) {
        if (FMLCommonHandler.instance().getSide().isClient())
            return;

        try {
            final ConfigSection.DB dbConfig = this.config.get().database;

            final HikariConfig config = new HikariConfig();
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", dbConfig.host, dbConfig.port, dbConfig.database));
            config.setUsername(dbConfig.username);
            config.setPassword(dbConfig.password);
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.addDataSourceProperty("useUnicode","true");
            config.addDataSourceProperty("characterEncoding","utf8");

            dataSource = new HikariDataSource(config);
            dataSource.validate();
        } catch (Exception e) {
            LOGGER.error("Error during connection to DB", e);
        }
    }

    void logCommand(ICommandSender sender, String cmd, String text) {
        threadPool.submit(() -> asyncLog(sender, cmd, text));
    }

    void logCommand(EntityLivingBase entity, String cmd, String text) {
        threadPool.submit(() -> asyncLog(entity, cmd, text));
    }

    private void asyncLog(ICommandSender sender, String cmd, String text) {
        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                LOGGER.error("Can't get connection to DB!");
                return;
            }

            final ConfigSection cfg = config.get();
            final String query = "INSERT INTO `" + cfg.chat_table +
                    "` (`chara`,`uuid`,`time`,`world`,`X`,`Y`,`Z`,`command`,`text`) " +
                    "VALUES (?,?,?,?,?,?,?,?,?);";

            final String uuid = (sender instanceof EntityPlayerMP)
                    ? ((EntityPlayerMP) sender).getUniqueID().toString()
                    : "";
            final long timeSecs = LocalDateTime.now(cfg.timezone).toEpochSecond(ZoneOffset.UTC);
            final ChunkCoordinates coord = sender.getPlayerCoordinates();

            final PreparedStatement s = conn.prepareStatement(query);
            s.setString(1, sender.getCommandSenderName());
            s.setString(2, uuid);
            s.setLong(3, timeSecs * 1000);
            s.setString(4, sender.getEntityWorld().getWorldInfo().getWorldName());
            s.setInt(5, coord.posX);
            s.setInt(6, coord.posY);
            s.setInt(7, coord.posZ);
            s.setString(8, cmd);
            s.setString(9, text);
            s.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to send log to the database!", e);
        }
    }

    private void asyncLog(EntityLivingBase entity, String cmd, String text) {
        try (Connection conn = dataSource.getConnection()) {
            if (conn == null) {
                LOGGER.error("Can't get connection to DB!");
                return;
            }

            final ConfigSection cfg = config.get();
            final String query = "INSERT INTO `" + cfg.chat_table +
                    "` (`chara`,`uuid`,`time`,`world`,`X`,`Y`,`Z`,`command`,`text`) " +
                    "VALUES (?,?,?,?,?,?,?,?,?);";

            final String uuid = entity instanceof EntityPlayer ? entity.getUniqueID().toString() : "";
            final long timeSecs = LocalDateTime.now(cfg.timezone).toEpochSecond(ZoneOffset.UTC);

            final PreparedStatement s = conn.prepareStatement(query);
            s.setString(1, entity.getCommandSenderName());
            s.setString(2, uuid);
            s.setLong(3, timeSecs * 1000);
            s.setString(4, entity.worldObj.getWorldInfo().getWorldName());
            s.setInt(5, MathHelper.floor_double(entity.posX));
            s.setInt(6, MathHelper.floor_double(entity.posY));
            s.setInt(7, MathHelper.floor_double(entity.posZ));
            s.setString(8, cmd);
            s.setString(9, text);
            s.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to send log to the database!", e);
        }
    }

    public static class ConfigSection {
        DB database = new DB();
        String chat_table = "chat_logs";
        ZoneId timezone = ZoneId.systemDefault();

        static class DB {
            String host = "localhost";
            int port = 3306;
            String database = "ortega";
            String username = "r00t";
            String password = "swordfish";
        }
    }
}
