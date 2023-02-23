package msifeed.mc.more.crabs.tags;

import com.google.gson.reflect.TypeToken;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import msifeed.mc.sys.config.ConfigBuilder;
import msifeed.mc.sys.config.JsonConfig;
import msifeed.mc.sys.utils.ChatUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.lwjgl.input.Keyboard;
import scala.swing.event.Key;

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

    private final JsonConfig<HashMap<String, TagDefinition>> tags = ConfigBuilder.of(tagType, "tags.json")
            .create();

    private final JsonConfig<HashMap<String, List<String>>> itemTags = ConfigBuilder.of(itemTagType, "item-tags.json")
            .create();

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        try {
            final ItemStack itemStack = event.itemStack;
            final Item item = itemStack.getItem();
            final String registryName = Item.itemRegistry.getNameForObject(item);
            final List<String> tagList = new ArrayList<>();
            final List<String> configList = itemTags.get().get(registryName);

            if (configList != null) {
                tagList.addAll(configList);
            }

            if (!tagList.isEmpty()) {
                displayTags(event.toolTip, tagList);
            }
        } catch (Exception ignored) {

        }
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
