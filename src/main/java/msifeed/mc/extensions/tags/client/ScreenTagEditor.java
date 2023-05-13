package msifeed.mc.extensions.tags.client;

import msifeed.mc.extensions.tags.Tags;
import msifeed.mc.mellow.layout.GridLayout;
import msifeed.mc.mellow.layout.ListLayout;
import msifeed.mc.mellow.utils.SizePolicy;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.button.Button;
import msifeed.mc.mellow.widgets.button.ButtonLabel;
import msifeed.mc.mellow.widgets.droplist.DropList;
import msifeed.mc.mellow.widgets.scroll.ScrollArea;
import msifeed.mc.mellow.widgets.text.Label;
import msifeed.mc.extensions.tags.TagsRpc;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ScreenTagEditor extends Widget {

    private final List<String> tags;
    private final Widget tagsList;

    public ScreenTagEditor(ItemStack itemStack) {
        tags = Tags.INSTANCE.get(itemStack);

        setLayout(ListLayout.VERTICAL);

        final ScrollArea scrollArea = new ScrollArea();
        scrollArea.setSizePolicy(SizePolicy.Policy.MAXIMUM, SizePolicy.Policy.FIXED);
        scrollArea.setSizeHint(100, 100);

        for (String tag : Tags.INSTANCE.tags.get().keySet()) {
            final ButtonLabel button = new ButtonLabel(tag);
            button.setClickCallback(() -> addTag(tag));
            scrollArea.addChild(button);
        }

        addChild(scrollArea);

        //final DropList<String> tag = new DropList<>(new ArrayList<>(Tags.INSTANCE.tags.get().keySet()));
        //tag.setSelectCallback(this::addTag);
        //addChild(tag);

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
