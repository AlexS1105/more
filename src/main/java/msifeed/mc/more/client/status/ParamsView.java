package msifeed.mc.more.client.status;

import msifeed.mc.mellow.layout.GridLayout;
import msifeed.mc.mellow.layout.ListLayout;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.text.Label;
import msifeed.mc.more.crabs.action.effects.Buff;
import msifeed.mc.more.crabs.action.effects.Effect;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.combat.PotionsHandler;
import msifeed.mc.more.crabs.utils.CombatAttribute;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;

import java.util.List;
import java.util.stream.Stream;

class ParamsView extends Widget {
    private final EntityLivingBase entity;
    private final Character character;
    private final boolean editable;

    ParamsView(EntityLivingBase entity, Character character, boolean editable) {
        this.entity = entity;
        this.character = character;
        this.editable = editable;

        setLayout(ListLayout.VERTICAL);

        refill();
    }

    public void refill() {
        clearChildren();

        final Widget grid = new Widget();
        grid.setLayout(new GridLayout(2));
        addChild(grid);

        if (editable) {
            grid.addChild(new Label(L10n.tr("more.gui.status.status.estitence")));
            grid.addChild(new Label(Integer.toString(character.estitence)));

            final int sin = character.sin;
            final String sinLevel = L10n.tr("more.status.sin." +
                    (sin < 0 ? "-1" : sin > 0 ? "1" : "0"));

            grid.addChild(new Label(L10n.tr("more.gui.status.status.sin")));
            grid.addChild(new Label(sinLevel + (sin > 0 ? " (" + sin + ")" : "")));
        }

        if (entity != Minecraft.getMinecraft().thePlayer)
            fillEnemy();

//        if (editable)
//            fillEditable();
//        else
//            fillNonEditable();
    }

    private void fillEnemy() {
        CombatAttribute.get(entity).ifPresent(context -> {
            final List<Effect> passiveBuffs = PotionsHandler.convertPassiveEffects(entity);
            if (!context.buffs.isEmpty() || !passiveBuffs.isEmpty()) {
                addChild(new Label("Buffs:"));

                final Widget buffs = new Widget();
                buffs.setLayout(ListLayout.VERTICAL);
                buffs.getMargin().left = 4;
                addChild(buffs);

                Stream.concat(
                        context.buffs.stream().map(ParamsView::formatBuff),
                        passiveBuffs.stream().map(ParamsView::formatPassiveBuff))
                        .forEach(s -> buffs.addChild(new Label(s)));
            }
        });
    }

    private static String formatBuff(Buff b) {
        final int counter = b.pause > 0 ? -b.pause : b.steps;
        return String.format("%2d - %s", counter, b.effect.format());
    }

    private static String formatPassiveBuff(Effect e) {
        return "** - " + e.format();
    }

    private void fillNonEditable() {
        setLayout(new GridLayout(2));
    }
}
