package msifeed.mc.more.crabs.tags.client;

import msifeed.mc.mellow.layout.GridLayout;
import msifeed.mc.mellow.layout.ListLayout;
import msifeed.mc.mellow.utils.SizePolicy;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.button.Button;
import msifeed.mc.mellow.widgets.button.ButtonLabel;
import msifeed.mc.mellow.widgets.droplist.DropList;
import msifeed.mc.mellow.widgets.text.Label;
import msifeed.mc.more.crabs.tags.Tags;
import msifeed.mc.more.crabs.tags.TagsRpc;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.item.ItemStack;
import scala.actors.threadpool.Arrays;

import java.util.List;
import java.util.stream.Collectors;

public class ScreenTagEditor extends Widget {

    private final List<String> tags;
    private final Widget tagsList;

    public ScreenTagEditor(ItemStack itemStack) {
        tags = Tags.INSTANCE.get(itemStack);

        setLayout(ListLayout.VERTICAL);

        final DropList<String> tag = new DropList<>(Arrays.asList(Tags.INSTANCE.tags.get().keySet().toArray()));
        tag.setSelectCallback(this::addTag);
        addChild(tag);

        addChild(new Label("Current Tags:"));

        tagsList = new Widget();
        tagsList.setLayout(new GridLayout());
        addChild(tagsList);

        updateList();

        final ButtonLabel applyBtn = new ButtonLabel(L10n.tr("more.gui.apply"));
        applyBtn.setSizePolicy(SizePolicy.Policy.MINIMUM, SizePolicy.Policy.PREFERRED);
        applyBtn.setClickCallback(() -> TagsRpc.updateTags(tags));
        addChild(applyBtn);
    }

    private void addTag(String tag) {
        if (tags.contains(tag)) {
            return;
        }

        tags.add(tag);

        updateList();
    }

    private void removeTag(String tag) {
        tags.remove(tag);

        updateList();
    }

    private void updateList() {
        tagsList.clearChildren();

        tags.sort(String::compareTo);

        for (String tag : tags) {
            final Button deleteButton = new ButtonLabel(tag.toUpperCase());
            deleteButton.setClickCallback(() -> removeTag(tag));
            tagsList.addChild(deleteButton);
        }
    }
}
