package msifeed.mc.sys.rpc;

import com.google.common.primitives.Primitives;
import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public enum RpcCodec {
    INSTANCE;

    private final ArrayList<TypeCodec<?>> codecsById = new ArrayList<>(Byte.MAX_VALUE);
    private final HashMap<Class<?>, TypeCodec<?>> codecsByType = new HashMap<>();

    RpcCodec() {
        addType(Boolean.class, ByteBuf::writeBoolean, ByteBuf::readBoolean);
        addType(Byte.class, (BiConsumer<ByteBuf, Byte>) ByteBuf::writeByte, ByteBuf::readByte);
        addType(Short.class, (BiConsumer<ByteBuf, Short>) ByteBuf::writeShort, ByteBuf::readShort);
        addType(Integer.class, ByteBuf::writeInt, ByteBuf::readInt);
        addType(Long.class, ByteBuf::writeLong, ByteBuf::readLong);
        addType(Float.class, ByteBuf::writeFloat, ByteBuf::readFloat);
        addType(Double.class, ByteBuf::writeDouble, ByteBuf::readDouble);
        addType(String.class, ByteBufUtils::writeUTF8String, ByteBufUtils::readUTF8String);
        addType(NBTTagCompound.class, ByteBufUtils::writeTag, ByteBufUtils::readTag);

        addType(byte[].class, RpcCodec::writeCompressed, RpcCodec::readCompressed);

        addType(UUID.class,
                (buf, uuid) -> {
                    buf.writeLong(uuid.getMostSignificantBits());
                    buf.writeLong(uuid.getLeastSignificantBits());
                },
                buf -> new UUID(buf.readLong(), buf.readLong()));

        addType(IChatComponent.class,
                (buf, comp) -> ByteBufUtils.writeUTF8String(buf, IChatComponent.Serializer.func_150696_a(comp)),
                buf -> IChatComponent.Serializer.func_150699_a(ByteBufUtils.readUTF8String(buf)));
        addAlias(ChatComponentStyle.class, IChatComponent.class);
        addAlias(ChatComponentText.class, IChatComponent.class);
        addAlias(ChatComponentTranslation.class, IChatComponent.class);
    }

    public boolean hasCodec(Class<?> c) {
        return codecsByType.containsKey(c);
    }

    public <T> void encode(ByteBuf buf, T obj) {
        final Class<?> objType = obj.getClass();
        final TypeCodec<T> codec = (TypeCodec<T>) codecsByType.get(objType);
        if (codec == null)
            throw new RuntimeException(String.format("Unknown codec for type '%s'", objType.getName()));
//        if (!codec.type.isAssignableFrom(objType))
//            throw new RuntimeException(String.format("Tried to encode '%s' with codec for '%s'", objType.getName(), codec.type.getName()));
        buf.writeByte(codec.id);
        codec.encoder.accept(buf, obj);
    }

    public Object decode(ByteBuf buf) {
        final int id = buf.readByte();
        if (id < 0 || id >= codecsById.size())
            throw new RuntimeException(String.format("Invalid codec id '%d'", id));
        final TypeCodec<?> codec = codecsById.get(id);
        return codec.decoder.apply(buf);
    }

    public <T> void addType(Class<T> type, BiConsumer<ByteBuf, T> encoder, Function<ByteBuf, T> decoder) {
        if (hasCodec(type))
            throw new RuntimeException(String.format("Duplicate codec for type '%s'", type.getName()));

        final int id = codecsById.size();
        final TypeCodec<T> codec = new TypeCodec<>(id, type, encoder, decoder);
        codecsById.add(codec);
        codecsByType.put(type, codec);

        if (Primitives.isWrapperType(type))
            codecsByType.put(Primitives.unwrap(type), codec);
    }

    public <T> void addAlias(Class<? extends T> type, Class<T> alias) {
        final TypeCodec<T> codec = (TypeCodec<T>) codecsByType.get(alias);
        if (codec == null)
            throw new RuntimeException(String.format("Unknown codec for alias '%s'", alias.getName()));

        codecsByType.put(type, codec);
    }

    private static void writeCompressed(ByteBuf buf, byte[] bb) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final DeflaterOutputStream out = new DeflaterOutputStream(bos);
            out.write(bb);
            out.close();

            buf.writeInt(bos.size());
            buf.writeBytes(bos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("failed to deflate bytes");
        }
    }

    private static byte[] readCompressed(ByteBuf buf) {
        try {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final InflaterOutputStream out = new InflaterOutputStream(bos);
            buf.readBytes(out, buf.readInt());
            out.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("failed to inflate bytes");
        }
    }

    private static class TypeCodec<T> {
        public final int id;
        public final Class<T> type;
        public final BiConsumer<ByteBuf, T> encoder;
        public final Function<ByteBuf, T> decoder;

        TypeCodec(int id, Class<T> type, BiConsumer<ByteBuf, T> enc, Function<ByteBuf, T> dec) {
            this.id = id;
            this.type = type;
            this.encoder = enc;
            this.decoder = dec;
        }
    }
}
