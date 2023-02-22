package msifeed.mc.sys.rpc;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import io.netty.channel.ChannelFutureListener;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RpcChannel {
    private final EnumMap<Side, FMLEmbeddedChannel> channels;
    private final HashMap<String, Handler> handlers = new HashMap<>();
    private final RpcCodec codec;
    private final Logger logger;

    public RpcChannel(String channel) {
        this(channel, new RpcCodec());
    }

    public RpcChannel(String channel, RpcCodec codec) {
        this.channels = NetworkRegistry.INSTANCE.newChannel(channel, new RpcPacketCodec(codec));
        this.codec = codec;
        this.logger = LogManager.getLogger("RPC:" + channel);

        final String type = channels.get(Side.SERVER).findChannelHandlerNameForType(RpcPacketCodec.class);
        channels.get(Side.SERVER).pipeline().addAfter(type, "RpcChannel", new RpcChannelHandler(this, Side.SERVER));
        channels.get(Side.CLIENT).pipeline().addAfter(type, "RpcChannel", new RpcChannelHandler(this, Side.CLIENT));
    }

    public void sendToAll(String method, Object... args) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        channels.get(Side.SERVER).writeAndFlush(new RpcMessage(method, args)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendTo(EntityPlayerMP player, String method, Object... args) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channels.get(Side.SERVER).writeAndFlush(new RpcMessage(method, args)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendToAllAround(NetworkRegistry.TargetPoint point, String method, Object... args) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        channels.get(Side.SERVER).writeAndFlush(new RpcMessage(method, args)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendToDimension(int dimensionId, String method, Object... args) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionId);
        channels.get(Side.SERVER).writeAndFlush(new RpcMessage(method, args)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void sendToServer(String method, Object... args) {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeAndFlush(new RpcMessage(method, args)).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

    public void register(Object obj) {
        final Class<?> clazz = obj.getClass();
        for (Method m : clazz.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(RpcMethodHandler.class))
                continue;
            final String methodName = m.getAnnotation(RpcMethodHandler.class).value();

            if (!m.isAccessible() && !Modifier.isPublic(m.getModifiers()))
                throw new RuntimeException(String.format("RPC method '%s::%s' is not accessible", clazz.getName(), m.getName()));

            final Class<?>[] types = m.getParameterTypes();

            final boolean hasContext = types.length > 0 && types[0] == RpcContext.class;
            final int argsStart = hasContext ? 1 : 0;
            for (int i = argsStart; i < types.length; ++i)
                if (!codec.hasCodecForType(types[i]))
                    throw new RuntimeException(String.format("RPC method '%s': param %d (%s) is not supported", methodName, i + 1, types[i].getSimpleName()));

            handlers.put(methodName, new Handler(obj, m, hasContext));
        }
    }

    void invoke(RpcMessage message, INetHandler netHandler, Side side) {
        final Handler handler = handlers.get(message.method);
        if (handler == null) return;

        try {
            if (handler.hasContext)
                handler.invoke(message.args, new RpcContext(this, netHandler, side));
            else
                handler.invoke(message.args);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            logger.error("Method '{}' excepted: {} at {}",
                    message.method, cause.toString(), cause.getStackTrace()[0].toString());

            if (!(cause instanceof RpcException)) {
                final String s = String.format("Method '%s' excepted: %s at %s",
                        message.method, cause.toString(), cause.getStackTrace()[0].toString());
                final IChatComponent cc = new ChatComponentText(s);
                cc.getChatStyle().setColor(EnumChatFormatting.RED);

                if (side.isServer())
                    ((NetHandlerPlayServer) netHandler).playerEntity.addChatMessage(cc);
                else
                    Minecraft.getMinecraft().thePlayer.addChatMessage(cc);
            }
        } catch (Throwable e) {
            logger.error("Method '{}' excepted: {} at {}",
                    message.method, e.toString(), e.getStackTrace()[0].toString());
        }
    }

    private class Handler {
        private final Object object;
        private final Method method;
        private final boolean hasContext;

        Handler(Object o, Method m, boolean hasContext) {
            this.object = o;
            this.method = m;
            this.hasContext = hasContext;
        }

        void invoke(Object[] args, RpcContext ctx) throws Exception {
            final Object[] argsWithCtx = new Object[args.length + 1];
            argsWithCtx[0] = ctx;
            System.arraycopy(args, 0, argsWithCtx, 1, args.length);

            invoke(argsWithCtx);
        }

        void invoke(Object[] args) throws Exception {
            try {
                method.invoke(object, args);
            } catch (IllegalArgumentException e) {
                final String expected = Stream.of(args)
                        .map(o -> o.getClass().getSimpleName())
                        .collect(Collectors.joining(","));
                final String actual = Stream.of(method.getParameterTypes())
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(","));
                logger.error("Method '{}' called with invalid arguments. Expected types: '{}', actual types: '{}'", method, expected, actual);
            }
//            catch (RpcException e) {
//                logger.error("Method '{}' failed with rpc error: {}", method, e);
//                e.send();
//            } catch (Exception e) {
//                logger.error("Method '{}' failed with exception: {}", method, e.getCause());
//                logger.throwing(e.getCause());
//            }
        }
    }
}
