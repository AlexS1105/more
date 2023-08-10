package msifeed.mc.more.client.status;

import msifeed.mc.mellow.layout.GridLayout;
import msifeed.mc.mellow.layout.ListLayout;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.basic.Separator;
import msifeed.mc.mellow.widgets.button.Checkbox;
import msifeed.mc.mellow.widgets.text.Label;
import msifeed.mc.mellow.widgets.text.TextInput;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.character.Trauma;
import msifeed.mc.more.crabs.utils.MetaAttribute;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.entity.EntityLivingBase;

class IllnessView extends Widget {
    private final Character character;
    private final boolean gmEditor;

    IllnessView(Character character, boolean gmEditor) {
        this.character = character;
        this.gmEditor = gmEditor;
        refill();
    }

    public void refill() {
        clearChildren();
        if (gmEditor)
            fillEditable();
        else
            fillNonEditable();
    }

    private void fillEditable() {
        setLayout(ListLayout.VERTICAL);

        final Widget info = new Widget();
        info.setLayout(ListLayout.VERTICAL);

        if (character.illness.cured())
            info.addChild(new Label(L10n.tr("more.gui.status.illness.cured")));
        if (character.illness.lost())
            info.addChild(new Label(L10n.tr("more.gui.status.illness.lost")));
        if (character.illness.debuff() != 0)
            info.addChild(new Label(L10n.fmt("more.gui.status.illness.debuff", character.illness.debuff())));

        if (!info.getChildren().isEmpty()) {
            addChild(info);
            addChild(new Separator());
        }

        final Widget params = new Widget();
        params.setLayout(new GridLayout());
        addChild(params);

        if (gmEditor) {
            params.addChild(new Label(L10n.tr("more.gui.status.illness.limit_visible")));
            final Checkbox checkbox = new Checkbox(character.illness.limitVisible);
            checkbox.setCallback(b -> character.illness.limitVisible = b);
            params.addChild(checkbox);

            params.addChild(new Label(L10n.tr("more.gui.status.illness.limit")));
            final TextInput limitInput = new TextInput();
            limitInput.getSizeHint().x = 25;
            limitInput.setFilter(s -> TextInput.isUnsignedIntBetween(s, 0, 999));
            limitInput.setCallback(s -> character.illness.limit = (short) limitInput.getInt());
            limitInput.setText(String.valueOf(character.illness.limit));
            params.addChild(limitInput);
        } else if (character.illness.limitVisible) {
            params.addChild(new Label(L10n.tr("more.gui.status.illness.limit")));
            params.addChild(new Label(String.valueOf(character.illness.limit)));
        }

        params.addChild(new Label(L10n.tr("more.gui.status.illness")));
        final TextInput illnessInput = new TextInput();
        illnessInput.getSizeHint().x = 25;
        illnessInput.setFilter(s -> TextInput.isUnsignedIntBetween(s, 0, 999));
        illnessInput.setCallback(s -> character.illness.illness = (short) illnessInput.getInt());
        illnessInput.setText(String.valueOf(character.illness.illness));
        params.addChild(illnessInput);


        params.addChild(new Label(L10n.tr("more.gui.status.illness.treatment")));
        final TextInput treatmentInput = new TextInput();
        treatmentInput.getSizeHint().x = 25;
        treatmentInput.setFilter(s -> TextInput.isUnsignedIntBetween(s, 0, 999));
        treatmentInput.setCallback(s -> character.illness.treatment = (short) treatmentInput.getInt());
        treatmentInput.setText(String.valueOf(character.illness.treatment));
        params.addChild(treatmentInput);

        addChild(new Separator());
        final Widget traumas = new Widget();
        traumas.setLayout(new GridLayout(3));
        addChild(traumas);

        for (Trauma t : Trauma.values()) {
            addTraumaParam(traumas, character, t, true);
        }
    }

    private void fillNonEditable() {
        setLayout(new GridLayout(5));

        if (character.illness.cured()) {
            addChild(new Label(L10n.tr("more.gui.status.illness.cured")));
            addChild(new Widget());
        }

        if (character.illness.lost()) {
            addChild(new Label(L10n.tr("more.gui.status.illness.lost")));
            addChild(new Widget());
        }

        if (character.illness.debuff() != 0)
            addChild(new Label(L10n.fmt("more.gui.status.illness.debuff", character.illness.debuff())));

        if (gmEditor || character.illness.limitVisible) {
            addChild(new Label(L10n.tr("more.gui.status.illness.limit")));
            addChild(new Label(String.valueOf(character.illness.limit)));
        }

        addChild(new Label(L10n.tr("more.gui.status.illness")));
        addChild(new Label(String.valueOf(character.illness.illness)));
        addChild(new Label(L10n.tr("more.gui.status.illness.treatment")));
        addChild(new Label(String.valueOf(character.illness.treatment)));


        for (Trauma t : Trauma.values()) {
            addTraumaParam(this, character, t, gmEditor);
        }
    }

    private void addTraumaParam(Widget traumas, Character character, Trauma trauma, boolean editable) {
        final Widget pair = new Widget();
        pair.setLayout(ListLayout.HORIZONTAL);
        traumas.addChild(pair);

        final Label label = new Label(trauma.trShort());
        label.getSizeHint().x = 40;
        label.getPos().y = 1;
        pair.addChild(label);

        final int traumaValue = character.traumas.getOrDefault(trauma, 0);

        final TextInput input = new TextInput();
        input.getSizeHint().x = 16;

        input.setText(Integer.toString(traumaValue));
        input.setFilter(s -> TextInput.isSignedIntBetween(s, 0, 99));
        input.setCallback(s -> {
            int value = s.isEmpty() ? 0 : Integer.parseInt(s);
            character.traumas.put(trauma, gmEditor ? value : Math.max(value, traumaValue));
        });
        pair.addChild(input);
    }
}
