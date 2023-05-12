package msifeed.mc.more.client.status;

import msifeed.mc.mellow.layout.GridLayout;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.button.Checkbox;
import msifeed.mc.mellow.widgets.text.Label;
import msifeed.mc.mellow.widgets.text.TextInput;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

class OtherView extends Widget {
    private final Character character;
    private final boolean editable;
    private EntityLivingBase entity;

    OtherView(Character character, EntityLivingBase entity, boolean editable) {
        this.character = character;
        this.editable = editable;
        this.entity = entity;

        setLayout(new GridLayout());
        getMargin().set(2, 0);
        refill();
    }

    public void refill() {
        clearChildren();
        if (editable)
            fillEditable();
        else
            fillNonEditable();
    }

    private void fillEditable() {
        clearChildren();

        addChild(new Label(L10n.tr("more.gui.status.other.name")));
        final TextInput nameInput = new TextInput();
//        nameInput.getSizeHint().x = 300;
        nameInput.getSizeHint().x = 100;
        nameInput.setMaxLineWidth(300);
        nameInput.setText(String.valueOf(character.name));
        nameInput.setFilter(TextInput::isValidName);
        nameInput.setCallback(s -> character.name = s);
        addChild(nameInput);

        addChild(new Label(L10n.tr("more.gui.status.other.wiki")));
        final TextInput wikiInput = new TextInput();
        wikiInput.setPlaceholderText("Название страницы");
        wikiInput.getSizeHint().x = 100;
        wikiInput.setMaxLineWidth(300);
        wikiInput.setText(String.valueOf(character.wikiPage));
        wikiInput.setCallback(s -> character.wikiPage = s);
        addChild(wikiInput);

        addChild(new Label(L10n.tr("more.gui.status.other.visibleOnMap")));
        final Checkbox visibleOnMap = new Checkbox(character.visibleOnMap);
        visibleOnMap.setCallback(b -> character.visibleOnMap = b);
        addChild(visibleOnMap);
    }

    private void fillNonEditable() {
        clearChildren();

        if (entity instanceof EntityPlayer) {
            addChild(new Label(L10n.tr("more.gui.status.other.wiki")));
            addChild(new CharacterPageUrlLabel(entity.getCommandSenderName()));
        }
    }
}
