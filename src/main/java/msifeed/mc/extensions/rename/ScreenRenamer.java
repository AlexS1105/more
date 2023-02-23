package msifeed.mc.extensions.rename;

import msifeed.mc.commons.traits.Trait;
import msifeed.mc.genesis.items.IItemTemplate;
import msifeed.mc.mellow.layout.GridLayout;
import msifeed.mc.mellow.layout.ListLayout;
import msifeed.mc.mellow.mc.MellowGuiScreen;
import msifeed.mc.mellow.utils.SizePolicy;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.button.ButtonLabel;
import msifeed.mc.mellow.widgets.button.FlatButtonLabel;
import msifeed.mc.mellow.widgets.tabs.TabArea;
import msifeed.mc.mellow.widgets.text.Label;
import msifeed.mc.mellow.widgets.text.TextInput;
import msifeed.mc.mellow.widgets.text.TextInputArea;
import msifeed.mc.mellow.widgets.window.Window;
import msifeed.mc.more.crabs.tags.client.ScreenTagEditor;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.sys.utils.ChatUtils;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ScreenRenamer extends MellowGuiScreen {
    private final TextInput titleInput = new TextInput();
    private final TextInputArea descInput = new TextInputArea(TextInputArea.NavMode.LINES);
    private Window addValueDialog = null;

    public ScreenRenamer(EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem();
        if (heldItem == null)
            return;

        final Window window = new Window();
        window.setTitle(L10n.tr("more.gui.renamer"));
        scene.addChild(window);

        final Widget content = window.getContent();
        content.setLayout(ListLayout.VERTICAL);

        final TabArea tabs = new TabArea();
        content.addChild(tabs);

        // // // //

        final Widget renameTab = new Widget();
        renameTab.setLayout(ListLayout.VERTICAL);
        tabs.addTab("Rename", renameTab);

        final boolean isGm = CharacterAttribute.hasAny(player, Trait.gm, Trait.__admin);

        if (isGm || true) {
            tabs.addTab("Tags", new ScreenTagEditor(heldItem));
        }

        renameTab.addChild(new Label(L10n.tr("more.gui.renamer.title")));
        renameTab.addChild(titleInput);
        titleInput.setMaxLineWidth(200);
        titleInput.setText(ChatUtils.intoAmpersandFormatting(heldItem.getDisplayName()));
        titleInput.setFilter(s -> !s.startsWith(" "));

        renameTab.addChild(new Label(L10n.tr("more.gui.renamer.desc")));
        renameTab.addChild(descInput);
        descInput.setLines(getItemDesc(heldItem)); // first line has title
        descInput.setSizeHint(200, 60);
        descInput.setSizePolicy(SizePolicy.Policy.MINIMUM, SizePolicy.Policy.MINIMUM);
        descInput.setMaxLineWidth(300);
        descInput.getController().setMaxLines(10);
        descInput.setColor(descInput.getColor());

        final Widget footer = new Widget();
        footer.setLayout(ListLayout.HORIZONTAL);
        renameTab.addChild(footer);

        final ButtonLabel applyBtn = new ButtonLabel(L10n.tr("more.gui.apply"));
        applyBtn.setSizePolicy(SizePolicy.Policy.MINIMUM, SizePolicy.Policy.PREFERRED);
        applyBtn.setClickCallback(() -> RenameRpc.rename(
                titleInput.getText(),
                descInput.toLineStream()
                        .map(ChatUtils::fromAmpersandFormatting)
                        .collect(Collectors.toList())
        ));
        footer.addChild(applyBtn);

        final ButtonLabel clearBtn = new ButtonLabel("Remove custom title & description");
        clearBtn.setClickCallback(() -> {
            RenameRpc.clear();
            closeGui();
        });
        footer.addChild(clearBtn);

        // // // //

        if (CharacterAttribute.has(player, Trait.gm)) {
            final Widget valuesTab = new Widget();
            valuesTab.setLayout(ListLayout.VERTICAL);
            tabs.addTab("Values", valuesTab);

            refillValues(valuesTab, player);
        }
    }

    private List<String> getItemDesc(ItemStack itemStack) {
        final List<String> customDesc = RenameProvider.getDescription(itemStack);
        if (!customDesc.isEmpty())
            return customDesc.stream()
                    .map(s -> s.substring(2)) // Remove &r prefix on each line
                    .map(ChatUtils::intoAmpersandFormatting)
                    .collect(Collectors.toList());

        if ((itemStack.getItem() instanceof IItemTemplate)) {
            final String[] desc = ((IItemTemplate) itemStack.getItem()).getUnit().desc;
            if (desc != null)
                return Stream.of(desc)
                        .map(ChatUtils::intoAmpersandFormatting)
                        .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    private void refillValues(Widget valuesTab, EntityPlayer player) {
        if (addValueDialog != null) {
            scene.removeChild(addValueDialog);
            addValueDialog = null;
        }
        valuesTab.clearChildren();

        final ItemStack heldItem = player.getHeldItem();
        if (heldItem == null)
            return;

        final Widget values = new Widget();
        values.setLayout(new GridLayout());
        valuesTab.addChild(values);

        for (Map.Entry<String, String> e : RenameProvider.getOverriddenValues(heldItem).entrySet()) {
            final FlatButtonLabel l = new FlatButtonLabel(e.getKey() + ": " + e.getValue());
            l.setClickCallback(() -> openValueDialog(e.getKey(), e.getValue()));
            values.addChild(l);

            final FlatButtonLabel r = new FlatButtonLabel("[x]");
            r.setClickCallback(() -> {
                RenameRpc.setValue(e.getKey(), null);
                refillValues(valuesTab, player);
            });
            values.addChild(r);
        }

        final ButtonLabel addBtn = new ButtonLabel("Add value");
        addBtn.setClickCallback(() -> openValueDialog("", ""));
        valuesTab.addChild(addBtn);

        final ButtonLabel refreshBtn = new ButtonLabel("Refresh");
        refreshBtn.setClickCallback(() -> refillValues(valuesTab, player));
        valuesTab.addChild(refreshBtn);
    }

    private void openValueDialog(String key, String value) {
        if (addValueDialog != null)
            return;
        addValueDialog = new Window();
        addValueDialog.setTitle("Add value");
        addValueDialog.setZLevel(addValueDialog.getZLevel() * 2);
        addValueDialog.getSizeHint().x = 200;
        addValueDialog.setSizePolicy(SizePolicy.Policy.MINIMUM, SizePolicy.Policy.PREFERRED);
        scene.addChild(addValueDialog);

        final Widget content = addValueDialog.getContent();

        final TextInput keyInput = new TextInput();
        keyInput.setPlaceholderText("Key");
        keyInput.setMaxLineWidth(200);
        keyInput.setText(key);
        content.addChild(keyInput);

        final TextInput valueInput = new TextInput();
        valueInput.setPlaceholderText("Value");
        valueInput.setMaxLineWidth(200);
        valueInput.setText(value);
        content.addChild(valueInput);

        final ButtonLabel submit = new ButtonLabel("Submit");
        submit.setClickCallback(() -> {
            RenameRpc.setValue(keyInput.getText(), valueInput.getText());
            addValueDialog.getParent().removeChild(addValueDialog);
            addValueDialog = null;
        });
        content.addChild(submit);
    }
}
