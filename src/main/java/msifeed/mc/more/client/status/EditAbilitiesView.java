package msifeed.mc.more.client.status;

import msifeed.mc.mellow.layout.GridLayout;
import msifeed.mc.mellow.layout.ListLayout;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.basic.Separator;
import msifeed.mc.mellow.widgets.text.Label;
import msifeed.mc.mellow.widgets.text.TextInput;
import msifeed.mc.more.crabs.character.Ability;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.sys.utils.L10n;

public class EditAbilitiesView extends Widget {
    private final Character character;
    public final boolean isGm;

    public EditAbilitiesView(Character character, boolean isGm) {
        this.character = character;
        this.isGm = isGm;

        setLayout(new GridLayout());
        refill();
    }

    public void refill() {
        clearChildren();

        for (final Ability a : Ability.values())
            addAbilityParam(character, a);

        if (isGm) {
            addChild(new Separator());
            addChild(new Separator());

            addChild(new Label(L10n.fmt("more.gui.status.status.intoxication")));
            final TextInput intoxicationInput = new TextInput();
            intoxicationInput.getSizeHint().x = 16;

            intoxicationInput.setText(Integer.toString(character.intoxication.size()));
            intoxicationInput.setFilter(s -> TextInput.isSignedIntBetween(s, 0, 15));
            intoxicationInput.setCallback(s -> {
                int value = s.isEmpty() ? 0 : Integer.parseInt(s);
                character.intoxication.clear();
                for (int i = 0; i < value; i++) {
                    character.intoxication.add(false);
                }
            });
            addChild(intoxicationInput);

            addChild(new Label(L10n.fmt("more.gui.status.status.attribution")));
            final TextInput attributionInput = new TextInput();
            attributionInput.getSizeHint().x = 16;

            attributionInput.setText(Integer.toString(character.attribution.size()));
            attributionInput.setFilter(s -> TextInput.isSignedIntBetween(s, 0, 15));
            attributionInput.setCallback(s -> {
                int value = s.isEmpty() ? 0 : Integer.parseInt(s);
                character.attribution.clear();
                for (int i = 0; i < value; i++) {
                    character.attribution.add(false);
                }
            });
            addChild(attributionInput);
        }
    }

    private void addAbilityParam(Character character, Ability a) {
        final Widget pair = new Widget();
        pair.setLayout(ListLayout.HORIZONTAL);
        addChild(pair);

        final Label label = new Label(a.trShort() + ":");
        label.getSizeHint().x = 25;
        label.getPos().y = 1;
        pair.addChild(label);

        final int abilityValue = character.abilities.getOrDefault(a, 0);

        if (this.isGm) {
            final TextInput input = new TextInput();
            input.getSizeHint().x = 16;

            input.setText(Integer.toString(abilityValue));
            input.setFilter(s -> TextInput.isSignedIntBetween(s, 1, 99));
            input.setCallback(s -> character.abilities.put(a, s.isEmpty() ? 1 : Integer.parseInt(s)));
            pair.addChild(input);
        } else {
            final Label valueLabel = new Label(Integer.toString(abilityValue));
            valueLabel.getSizeHint().x = 16;
            pair.addChild(valueLabel);
        }
    }
}