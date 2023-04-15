package msifeed.mc.more.client.morph;

import msifeed.mc.commons.traits.Trait;
import msifeed.mc.mellow.layout.GridLayout;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.text.Label;
import msifeed.mc.mellow.widgets.text.TextInput;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import net.minecraft.client.Minecraft;

class EditParamsView extends Widget {
    EditParamsView(Character character) {
        final Minecraft mc = Minecraft.getMinecraft();
        final boolean isGm = CharacterAttribute.hasAny(mc.thePlayer, Trait.gm, Trait.__admin);

        setLayout(new GridLayout());

        if (isGm) {
            addChild(new Label("Armor override"));
            final TextInput armorOverrideInput = new TextInput();
            armorOverrideInput.getSizeHint().x = 30;
            armorOverrideInput.setText(String.valueOf(character.armor));
            armorOverrideInput.setFilter(s -> TextInput.isUnsignedIntBetween(s, 0, 1000));
            armorOverrideInput.setCallback(s -> character.armor = armorOverrideInput.getInt());
            addChild(armorOverrideInput);

            addChild(new Label("Damage threshold"));
            final TextInput damageThresholdInput = new TextInput();
            damageThresholdInput.getSizeHint().x = 30;
            damageThresholdInput.setText(String.valueOf(character.damageThreshold));
            damageThresholdInput.setFilter(s -> TextInput.isUnsignedIntBetween(s, 0, 1000));
            damageThresholdInput.setCallback(s -> character.damageThreshold = damageThresholdInput.getInt());
            addChild(damageThresholdInput);
        }

        addChild(new Label("Fists damage"));
        final TextInput fistsDmgInput = new TextInput();
        fistsDmgInput.getSizeHint().x = 30;
        fistsDmgInput.setText(String.valueOf(character.fistsDamage));
        fistsDmgInput.setFilter(s -> TextInput.isUnsignedIntBetween(s, 0, 100));
        fistsDmgInput.setCallback(s -> character.fistsDamage = fistsDmgInput.getInt());
        addChild(fistsDmgInput);

        addChild(new Label("Estitence"));
        final TextInput estitenceInput = new TextInput();
        estitenceInput.getSizeHint().x = 30;
        estitenceInput.setText(String.valueOf(character.estitence));
        estitenceInput.setFilter(s -> TextInput.isUnsignedIntBetween(s, 1, 100));
        estitenceInput.setCallback(s -> character.estitence = estitenceInput.getInt());
        addChild(estitenceInput);

        addChild(new Label("Greyness"));
        final TextInput sinInput = new TextInput();
        sinInput.getSizeHint().x = 30;
        sinInput.setText(String.valueOf(character.sin));
        sinInput.setFilter(s -> TextInput.isUnsignedIntBetween(s, 0, 6));
        sinInput.setCallback(s -> character.sin = sinInput.getInt());
        addChild(sinInput);

        addChild(new Label("Health"));
        final TextInput healthInput = new TextInput();
        healthInput.getSizeHint().x = 30;
        healthInput.setText(String.valueOf(character.healthMod));
        healthInput.setFilter(TextInput::isUnsignedFloat);
        healthInput.setCallback(s -> character.healthMod = healthInput.getFloat());
        addChild(healthInput);
    }
}
