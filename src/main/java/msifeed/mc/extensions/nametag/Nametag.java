package msifeed.mc.extensions.nametag;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import msifeed.mc.Bootstrap;
import msifeed.mc.more.More;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.sys.rpc.RpcContext;
import msifeed.mc.sys.rpc.RpcMethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

import javax.annotation.Nullable;

public class Nametag {
    @SidedProxy(
            serverSide = "msifeed.mc.extensions.nametag.Nametag",
            clientSide = "msifeed.mc.extensions.nametag.NametagClient"
    )
    public static Nametag INSTANCE;

    static final String broadcastTyping = Bootstrap.MODID + ":nametags.broadcast";
    private static final String notifyTyping = Bootstrap.MODID + ":nametags.notify";

    public static void preInit() {
        More.RPC.register(INSTANCE);
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        FMLCommonHandler.instance().bus().register(INSTANCE);
    }

    public static void notifyTyping() {
        More.RPC.sendToServer(Nametag.notifyTyping, Minecraft.getMinecraft().thePlayer.getEntityId());
    }

    @RpcMethodHandler(notifyTyping)
    public void onNotifyTyping(RpcContext ctx, int id) {
        final EntityPlayerMP player = ctx.getServerHandler().playerEntity;
        final ChunkCoordinates coord = player.getPlayerCoordinates();
        final NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(
                player.dimension, coord.posX, coord.posY, coord.posZ, getSpeechRadius()
        );
        More.RPC.sendToAllAround(point, Nametag.broadcastTyping, id);
    }

    @SubscribeEvent
    public void onNameFormat(PlayerEvent.NameFormat event) {
        event.displayname = getPreferredName(event.entityPlayer);
    }

    protected String getPreferredName(EntityPlayer player) {
        final Character c = CharacterAttribute.require(player);
        return c.name.isEmpty() ? player.getCommandSenderName() : c.name;
    }

    protected @Nullable String getStatus(EntityPlayer player) {
        final Character c = CharacterAttribute.require(player);
        return c.status.isEmpty() ? null : c.status;
    }

    static int getSpeechRadius() {
        final int[] speechRadius = More.DEFINES.get().chat.speechRadius;
        final int mid = (speechRadius.length - 1) / 2;
        return speechRadius[mid];
    }
}
