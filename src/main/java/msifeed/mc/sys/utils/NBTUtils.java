package msifeed.mc.sys.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.List;

public final class NBTUtils {
    public static NBTTagCompound itemStackToNBT(ItemStack stack) {
        final NBTTagCompound tag = new NBTTagCompound();
        stack.writeToNBT(tag);
        return tag;
    }

    public static ItemStack itemStackFromNBT(NBTTagCompound tag) {
        if (tag == null)
            return null;
        return ItemStack.loadItemStackFromNBT(tag);
    }

    public static int[] packBooleanList(List<Boolean> list) {
        int[] result = new int[list.size()];

        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i) ? 1 : 0;
        }

        return result;
    }

    public static List<Boolean> unpackBooleanList(int[] bytes) {
        List<Boolean> result = new ArrayList<>(bytes.length);

        for (int b : bytes) {
            result.add(b == 1);
        }

        return result;
    }
}
