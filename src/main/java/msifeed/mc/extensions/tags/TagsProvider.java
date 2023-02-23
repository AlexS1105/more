package msifeed.mc.extensions.tags;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TagsProvider {
    public static void setTags(ItemStack itemStack, NBTTagCompound nbt) {
        if (!itemStack.hasTagCompound()) {
            itemStack.stackTagCompound = new NBTTagCompound();
        }

        itemStack.stackTagCompound.setTag("tags", nbt.getTag("tags"));
    }
}
