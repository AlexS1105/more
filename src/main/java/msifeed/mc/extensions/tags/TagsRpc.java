package msifeed.mc.extensions.tags;

import msifeed.mc.Bootstrap;
import msifeed.mc.more.More;
import msifeed.mc.sys.rpc.RpcContext;
import msifeed.mc.sys.rpc.RpcMethodHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.List;

public enum TagsRpc {
    INSTANCE;

    private static final String updateTags = Bootstrap.MODID + ":update_tags";

    public static void preInit() {
        More.RPC.register(INSTANCE);
    }

    public static void updateTags(List<String> tags) {
        final NBTTagCompound nbt = new NBTTagCompound();

        final NBTTagList tagsNbt = new NBTTagList();
        for (String tag : tags)
            tagsNbt.appendTag(new NBTTagString(tag));
        nbt.setTag("tags", tagsNbt);

        More.RPC.sendToServer(updateTags, nbt);
    }

    @RpcMethodHandler(updateTags)
    public void updateTags(RpcContext ctx, NBTTagCompound nbt) {
        final EntityPlayer sender = ctx.getServerHandler().playerEntity;
        final ItemStack itemStack = sender.getHeldItem();
        if (itemStack == null)
            return;

       TagsProvider.setTags(itemStack, nbt);
    }
}
