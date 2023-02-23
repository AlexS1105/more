package msifeed.mc.extensions.tags;

import com.google.gson.reflect.TypeToken;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import msifeed.mc.sys.config.ConfigBuilder;
import msifeed.mc.sys.config.JsonConfig;
import msifeed.mc.sys.utils.ChatUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public enum Tags {
    INSTANCE;

    private final TypeToken<HashMap<String, TagDefinition>> tagType = new TypeToken<HashMap<String, TagDefinition>>() {
    };
    private final TypeToken<HashMap<String, List<String>>> itemTagType = new TypeToken<HashMap<String, List<String>>>() {
    };

    public final JsonConfig<HashMap<String, TagDefinition>> tags = ConfigBuilder.of(tagType, "tags.json")
            .create();

    private final JsonConfig<HashMap<String, List<String>>> itemTags = ConfigBuilder.of(itemTagType, "item-tags.json")
            .create();

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        try {
            final List<String> tagList = get(event.itemStack);

            if (!tagList.isEmpty()) {
                displayTags(event.toolTip, tagList);
            }
        } catch (Exception ignored) {

        }
    }

    public List<String> get(ItemStack itemStack) {
        final Item item = itemStack.getItem();
        final String registryName = Item.itemRegistry.getNameForObject(item);
        final List<String> tagList = new ArrayList<>();
        final List<String> configList = itemTags.get().get(registryName);

        if (itemStack.hasTagCompound() && itemStack.stackTagCompound.hasKey("tags")) {
            final NBTTagList nbtTagList = itemStack.getTagCompound().getTagList("tags", 8);

            for (int i = 0; i < nbtTagList.tagCount(); i++) {
                tagList.add(nbtTagList.getStringTagAt(i));
            }
        } else if (configList != null) {
            tagList.addAll(configList);
        }

        return tagList;
    }

    private void displayTags(List<String> toolTip, List<String> tagList) {
        for (String tag : tagList) {
            final TagDefinition tagDefinition = tags.get().get(tag);

            if (tagDefinition != null) {
                int index = Math.max(0, toolTip.size() - 1);
                toolTip.add(index++, ChatUtils.fromAmpersandFormatting(tagDefinition.name));

                if (tagDefinition.lines != null && !tagDefinition.lines.isEmpty()
                        && Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    toolTip.addAll(index, tagDefinition.lines.stream()
                        .map(ChatUtils::fromAmpersandFormatting)
                        .collect(Collectors.toList()));
                }
            }
        }
    }
}
