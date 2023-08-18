package msifeed.mc.extensions.rename;

import msifeed.mc.Bootstrap;
import msifeed.mc.more.More;
import msifeed.mc.sys.rpc.RpcContext;
import msifeed.mc.sys.rpc.RpcMethodHandler;
import msifeed.mc.sys.utils.ChatUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.List;

public enum RenameRpc {
    INSTANCE;

    private static final String rename = Bootstrap.MODID + ":rename";
    private static final String clear = Bootstrap.MODID + ":rename.clear";
    private static final String setValue = Bootstrap.MODID + ":rename.value.set";
    private static final String openRenameGui = Bootstrap.MODID + ":rename.gui";

    public static void preInit() {
        More.RPC.register(INSTANCE);
    }

    public static void rename(String title, List<String> desc, boolean isGm) {
        final NBTTagCompound nbt = new NBTTagCompound();
        if (!title.isEmpty())
            nbt.setString("t", title);

        final NBTTagList descNbt = new NBTTagList();
        for (String l : desc)
            descNbt.appendTag(new NBTTagString(ChatUtils.fromAmpersandFormatting("\u00A7r" + l)));
        nbt.setTag("d", descNbt);

        nbt.setBoolean("verified", isGm);

        More.RPC.sendToServer(rename, nbt);
    }

    @RpcMethodHandler(rename)
    public void onRename(RpcContext ctx, NBTTagCompound nbt) {
        final EntityPlayer sender = ctx.getServerHandler().playerEntity;
        final ItemStack itemStack = sender.getHeldItem();
        if (itemStack == null)
            return;

        if (nbt.hasKey("t")) {
            boolean verified = nbt.getBoolean("verified");
            String title = nbt.getString("t");
            if (!verified) {
                title += " &9[?]";
            }
            RenameProvider.setTitle(itemStack, title);
        }
        if (nbt.hasKey("d"))
            RenameProvider.setDescription(itemStack, nbt.getTagList("d", 8)); // 8 - string
    }

    public static void clear() {
        More.RPC.sendToServer(clear);
    }

    @RpcMethodHandler(clear)
    public void onClear(RpcContext ctx) {
        final EntityPlayer sender = ctx.getServerHandler().playerEntity;
        final ItemStack itemStack = sender.getHeldItem();
        if (itemStack == null)
            return;

        RenameProvider.clearAll(itemStack);
    }

    public static void setValue(String key, String value) {
        if (!key.isEmpty())
            More.RPC.sendToServer(setValue, key, value == null ? "[del]" : value);
    }

    @RpcMethodHandler(setValue)
    public void onSetValue(RpcContext ctx, String key, String value) {
        final EntityPlayer sender = ctx.getServerHandler().playerEntity;
        final ItemStack itemStack = sender.getHeldItem();
        if (itemStack == null)
            return;

        RenameProvider.setValue(itemStack, key, value.equals("[del]") ? null : value);
    }

    public static void openRenameGui(EntityPlayerMP player) {
        More.RPC.sendTo(player, openRenameGui);
    }

    @RpcMethodHandler(openRenameGui)
    public void onOpenRenameGui() {
        More.GUI_HANDLER.toggleRenamer();
    }
}
