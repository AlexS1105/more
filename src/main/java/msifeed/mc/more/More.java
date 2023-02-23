package msifeed.mc.more;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import msifeed.mc.Bootstrap;
import msifeed.mc.commons.defines.DefineCommand;
import msifeed.mc.commons.defines.Defines;
import msifeed.mc.commons.logs.ExternalLogs;
import msifeed.mc.commons.traits.TraitListCommand;
import msifeed.mc.commons.traits.TraitSetCommand;
import msifeed.mc.extensions.books.RemoteBookManager;
import msifeed.mc.extensions.chat.Speechat;
import msifeed.mc.extensions.environment.EnvironmentManager;
import msifeed.mc.extensions.invulnerability.TempInvulnerability;
import msifeed.mc.extensions.itemmeta.ItemMetaCommand;
import msifeed.mc.extensions.locks.Locks;
import msifeed.mc.extensions.mining.MiningNerf;
import msifeed.mc.extensions.mining.StaminaCommand;
import msifeed.mc.extensions.nametag.Nametag;
import msifeed.mc.extensions.noclip.NoclipCommand;
import msifeed.mc.extensions.noclip.NoclipRpc;
import msifeed.mc.extensions.rename.RenameCommand;
import msifeed.mc.extensions.rename.RenameRpc;
import msifeed.mc.extensions.tweaks.*;
import msifeed.mc.genesis.Genesis;
import msifeed.mc.more.client.common.GuiHandler;
import msifeed.mc.more.commands.ItemAttrCommand;
import msifeed.mc.more.commands.MoreCommand;
import msifeed.mc.more.commands.RollCommand;
import msifeed.mc.more.content.MoreItems;
import msifeed.mc.more.crabs.Crabs;
import msifeed.mc.more.crabs.crust.CrustSync;
import msifeed.mc.extensions.tags.TagsRpc;
import msifeed.mc.more.tools.ToolItems;
import msifeed.mc.sys.rpc.RpcChannel;
import net.minecraft.command.CommandHandler;
import net.minecraft.server.MinecraftServer;

public class More {
    @SidedProxy(
            serverSide = "msifeed.mc.genesis.Genesis",
            clientSide = "msifeed.mc.genesis.GenesisClient"
    )
    private static Genesis genesis;
    @SidedProxy(
            serverSide = "msifeed.mc.more.client.common.GuiHandler",
            clientSide = "msifeed.mc.more.client.common.GuiHandlerClient"
    )
    public static GuiHandler GUI_HANDLER;
    public static Defines DEFINES = new Defines();
    public static RpcChannel RPC = new RpcChannel(Bootstrap.MODID + ":rpc");

    private Crabs crabs = new Crabs();
    private ExternalLogs externalLogs = new ExternalLogs();
    private EntityControl entityControl = new EntityControl();
    private Speechat speechat = new Speechat();
    private Locks locks = new Locks();
    private TempInvulnerability invulnerability = new TempInvulnerability();
    private CrustSync crustSync = new CrustSync();

    public void preInit() {
        crabs.preInit();
        externalLogs.preInit();
        entityControl.preInit();
        speechat.preInit();
        locks.preInit();

        RemoteBookManager.preInit();
        RenameRpc.preInit();
        HealthModifier.preInit();
        NoclipRpc.preInit();
        Nametag.preInit();
        EnvironmentManager.preInit();
        MiningNerf.preInit();
        DisableSomeCraftingTables.preInit();
        TagsRpc.preInit();
    }

    public void init() {
        genesis.init();
        locks.init();
        invulnerability.init();

        RemoteBookManager.init();
        ToolItems.init();
        MoreItems.init();
    }

    public void postInit() {
        MakeFoodEdible.apply();
    }

    public void serverStarting(FMLServerStartingEvent event) {
        final MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        final CommandHandler handler = (CommandHandler) server.getCommandManager();

        handler.registerCommand(new DefineCommand());
        handler.registerCommand(new TraitListCommand());
        handler.registerCommand(new TraitSetCommand());
        handler.registerCommand(new MoreCommand());
        handler.registerCommand(new RenameCommand());
        handler.registerCommand(new UnstuckCommand());
        handler.registerCommand(new ItemMetaCommand());
        handler.registerCommand(new RollCommand());
        handler.registerCommand(new NoclipCommand());
        handler.registerCommand(new ItemAttrCommand());
        handler.registerCommand(new StaminaCommand());

        Speechat.registerCommands(handler);
        EnvironmentManager.registerCommands(handler);
    }
}
