package msifeed.mc.extensions.tags;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class TagsProvider {
    public static void setTags(ItemStack itemStack, NBTTagCompound nbt) {
        NBTTagCompound tagCompound;

        if (!itemStack.hasTagCompound()) {
            tagCompound = new NBTTagCompound();
        } else {
            tagCompound = itemStack.getTagCompound();
        }

        tagCompound.setTag("tags", nbt.getTag("tags"));
        itemStack.setTagCompound(tagCompound);
    }
}
