package msifeed.mc.more.client.combat;

import msifeed.mc.mellow.layout.FillLayout;
import msifeed.mc.mellow.layout.ListLayout;
import msifeed.mc.mellow.utils.SizePolicy;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.button.ButtonLabel;
import msifeed.mc.mellow.widgets.scroll.ScrollArea;
import msifeed.mc.mellow.widgets.spoiler.Spoiler;
import msifeed.mc.more.crabs.character.Ability;
import msifeed.mc.more.crabs.character.CharRpc;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.character.Skill;
import msifeed.mc.more.crabs.rolls.Modifiers;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.more.crabs.utils.MetaAttribute;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.entity.EntityLivingBase;

import java.awt.*;
import java.util.HashMap;

public class RollAbilityView extends Widget {
    private final EntityLivingBase entity;
    private final HashMap<Ability, Widget> statWidgets = new HashMap<>();
    private final ScrollArea scroll = new ScrollArea();

    RollAbilityView(EntityLivingBase entity) {
        this.entity = entity;

        setSizeHint(200, 160);
        setLayout(FillLayout.INSTANCE);
        setSizePolicy(SizePolicy.Policy.MINIMUM, SizePolicy.Policy.MINIMUM);
        addChild(scroll);

        refill();
    }

    private void refill() {
        if (!CharacterAttribute.get(entity).isPresent())
            return;

        final Character character = CharacterAttribute.get(entity).get();

        if (!MetaAttribute.get(entity).isPresent())
            return;

        final Modifiers modifiers = MetaAttribute.get(entity).get().modifiers;

        for (Ability ability : Ability.values()) {
            final Widget line = new Widget();
            line.setLayout(ListLayout.HORIZONTAL);

            final Widget widget = new Widget();
            widget.setLayout(ListLayout.VERTICAL);

            final ButtonLabel btn = new ButtonLabel(ability.trShort()
                    + String.format(" (%d)", character.abilities.get(ability) + modifiers.toAbility(ability)));
            btn.setClickCallback(() -> CharRpc.rollAbility(entity.getEntityId(), ability));
            btn.setSizePolicy(SizePolicy.Policy.MINIMUM, SizePolicy.Policy.MINIMUM);
            btn.setSizeHint(20, 0);
            line.addChild(btn);
            statWidgets.put(ability, widget);

            final Spoiler spoiler = new Spoiler(L10n.fmt("more.gui.combat.skills"), widget);
            spoiler.setSizeHint(300, 100);
            line.addChild(spoiler);

            scroll.addChild(line);
        }

        for (Skill skill : character.skills) {
            final Widget widget = statWidgets.get(skill.stat);

            final ButtonLabel btn = new ButtonLabel(skill.name
                    + String.format(" (%d)", skill.getBonus(character, modifiers)));

            if (skill.proficiency) {
                btn.setColor(Color.orange.getRGB());
            }

            btn.setClickCallback(() -> CharRpc.rollSkill(entity.getEntityId(), skill));

            widget.addChild(btn);
        }
    }
}
