package msifeed.mc.more.crabs.crust;

import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import msifeed.mc.more.crabs.character.Ability;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.sys.config.ConfigBuilder;
import msifeed.mc.sys.config.ConfigEvent;
import msifeed.mc.sys.config.JsonConfig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.concurrent.TimeUnit;

public class CrustSync {
    private static final Logger LOG = LogManager.getLogger("More.CrustSync");
    private JsonConfig<CrustSyncConfig> config = ConfigBuilder.of(CrustSyncConfig.class, "crust-sync.json")
            .create();

    private MongoCollection<CrustCharsheet> sheets;

    public CrustSync() {
//        if (FMLCommonHandler.instance().getSide().isServer()) {
        FMLCommonHandler.instance().bus().register(this);
        MinecraftForge.EVENT_BUS.register(this);
//        }
    }

    @SubscribeEvent
    public void onReloadDone(ConfigEvent.AfterUpdate event) {
//        if (FMLCommonHandler.instance().getSide().isClient())
//            return;

        final CrustSyncConfig cfg = config.get();
        if (cfg.disabled) return;

        final String conn = String.format("mongodb://%s:%s@%s:%d/%s",
                cfg.username, cfg.password,
                cfg.host, cfg.port,
                cfg.database);

        try {
            LOG.info("Try to connect to Keeper DB...");

            final MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(conn))
                    .applyToConnectionPoolSettings(builder -> builder.maxWaitTime(5, TimeUnit.SECONDS))
                    .codecRegistry(CodecRegistries.fromProviders(
                            PojoCodecProvider.builder()
                                    .register(CrustCharsheet.class)
                                    .build(),
                            new ValueCodecProvider()
                    ))
                    .build();
            sheets = MongoClients.create(settings)
                    .getDatabase(cfg.database)
                    .getCollection(cfg.collection, CrustCharsheet.class);
            LOG.info("Connection to Keeper DB is successful");
        } catch (Exception e) {
            LOG.error("Failed to connect to Keeper DB", e);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        System.out.println("CRUST onPlayerLogin");
        sync(event.player);
    }

    public void sync(EntityPlayer player) {
        if (sheets == null) return;

        final String username = player.getGameProfile().getName();
        LOG.info("Sync {} with Keeper DB...", username);

        final CrustCharsheet sheet;
        CrustCharsheet holdMySheet;
        try {
            holdMySheet = sheets.find(Filters.eq("character", username)).first();
        } catch (Exception e) {
            LOG.error("Failed to get info from Keeper", e);
            holdMySheet = null;
        }
        sheet = holdMySheet;

        // FIXME: add some kind of thread sync?

        if (sheet != null) LOG.info("Found entry: " + new Gson().toJson(sheet));
        else LOG.warn("Player entry is not found: " + username);

        CharacterAttribute.INSTANCE.update(player, character -> {
            System.out.println("CRUST CharacterAttribute.INSTANCE.update");

            if (sheet == null) return;

            character.loadedFromCrust = true; // always toggle after  load

            for (Ability a : Ability.values()) {
                final String crustKey = CrustCharsheet.ability2crust(a);
                character.abilities.put(a, sheet.stats.getOrDefault(crustKey, 0));
            }

            character.estitence = sheet.estitence;
        });
    }
}
