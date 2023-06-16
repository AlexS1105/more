package msifeed.mc.more.client.combat;

import msifeed.mc.mellow.layout.GridLayout;
import msifeed.mc.mellow.layout.ListLayout;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.text.Label;
import msifeed.mc.mellow.widgets.text.TextInput;
import msifeed.mc.more.crabs.character.Ability;
import msifeed.mc.more.crabs.meta.MetaInfo;
import msifeed.mc.more.crabs.meta.MetaRpc;
import msifeed.mc.more.crabs.rolls.Modifiers;
import msifeed.mc.more.crabs.utils.MetaAttribute;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.entity.EntityLivingBase;

class ModsView extends Widget {
    private final EntityLivingBase entity;

    ModsView(EntityLivingBase entity) {
        this.entity = entity;

        setLayout(new GridLayout());

        refill();
    }

    public void refill() {
        clearChildren();

        MetaAttribute.get(entity).ifPresent(meta -> {
            final Modifiers mods = meta.modifiers;

            addChild(new Label(L10n.tr("more.gui.combat.mods.damage")));
            final TextInput dmgInput = new TextInput();
            dmgInput.getSizeHint().x = 20;
            if (mods.damage != 0)
                dmgInput.setText(Integer.toString(mods.damage));
            dmgInput.setFilter(s -> TextInput.isSignedIntBetween(s, -99, 99));
            dmgInput.setCallback(s -> updateDamageMod(meta, s));
            addChild(dmgInput);

            addChild(new Label(L10n.tr("more.gui.combat.mods.roll")));
            final TextInput modInput = new TextInput();
            modInput.getSizeHint().x = 20;
            if (mods.roll != 0)
                modInput.setText(Integer.toString(mods.roll));
            modInput.setFilter(s -> TextInput.isSignedIntBetween(s, -99, 99));
            modInput.setCallback(s -> updateRollMod(meta, s));
            addChild(modInput);

            for (final Ability a : Ability.values())
                addAbilityMod(meta, a);
        });
    }

    private void addAbilityMod(MetaInfo meta, Ability a) {
        final Widget pair = new Widget();
        pair.setLayout(ListLayout.HORIZONTAL);
        addChild(pair);

        final Label label = new Label(a.trShort() + ":");
        label.getSizeHint().x = 25;
        label.getPos().y = 1;
        pair.addChild(label);

        final TextInput input = new TextInput();
        input.getSizeHint().x = 16;

        final int modValue = meta.modifiers.customToAbility(a);
        if (modValue != 0)
            input.setText(Integer.toString(modValue));
        input.setFilter(s -> TextInput.isSignedIntBetween(s, -99, 99));
        input.setCallback(s -> updateFeatMods(meta, a, s));
        pair.addChild(input);
    }

    private void updateDamageMod(MetaInfo meta, String s) {
        meta.modifiers.damage = (s.isEmpty() || s.equals("-")) ? 0 : Integer.parseInt(s);
        MetaRpc.updateMeta(entity.getEntityId(), meta);
    }

    private void updateRollMod(MetaInfo meta, String s) {
        meta.modifiers.roll = (s.isEmpty() || s.equals("-")) ? 0 : Integer.parseInt(s);
        MetaRpc.updateMeta(entity.getEntityId(), meta);
    }

    private void updateFeatMods(MetaInfo meta, Ability a, String s) {
        if (s.isEmpty() || s.equals("-"))
            meta.modifiers.abilities.remove(a);
        else
            meta.modifiers.abilities.put(a, Integer.parseInt(s));
        MetaRpc.updateMeta(entity.getEntityId(), meta);
    }
}