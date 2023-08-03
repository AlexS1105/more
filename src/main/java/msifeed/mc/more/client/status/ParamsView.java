package msifeed.mc.more.client.status;

import msifeed.mc.mellow.layout.*;
import msifeed.mc.mellow.utils.Margins;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.basic.Separator;
import msifeed.mc.mellow.widgets.basic.WrapWidget;
import msifeed.mc.mellow.widgets.button.Checkbox;
import msifeed.mc.mellow.widgets.button.Crossbox;
import msifeed.mc.mellow.widgets.text.Label;
import msifeed.mc.mellow.widgets.text.TextInput;
import msifeed.mc.mellow.widgets.text.WordwrapLabel;
import msifeed.mc.more.crabs.action.effects.Buff;
import msifeed.mc.more.crabs.action.effects.Effect;
import msifeed.mc.more.crabs.character.CharRpc;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.character.Limb;
import msifeed.mc.more.crabs.combat.PotionsHandler;
import msifeed.mc.more.crabs.meta.MetaRpc;
import msifeed.mc.more.crabs.utils.CombatAttribute;
import msifeed.mc.more.crabs.utils.MetaAttribute;
import msifeed.mc.sys.utils.L10n;
import msifeed.mc.sys.utils.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import org.apache.commons.lang3.text.WordUtils;
import scala.Int;

import java.util.*;
import java.util.stream.Stream;

class ParamsView extends Widget {
    private final EntityLivingBase entity;
    private final Character character;
    private final boolean editable;
    private final boolean viewingAsGM;

    ParamsView(EntityLivingBase entity, Character character, boolean editable, boolean viewingAsGM) {
        this.entity = entity;
        this.character = character;
        this.editable = editable;
        this.viewingAsGM = viewingAsGM;

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

        grid.addChild(new Label(L10n.tr("more.gui.status.status.current_status")));

        if (editable || viewingAsGM) {
            final TextInput statusInput = new TextInput();
            statusInput.getSizeHint().x = 100;
            statusInput.setMaxLineWidth(300);
            statusInput.setText(character.status);
            statusInput.setFilter((s) -> s.length() <= 32);
            statusInput.setCallback(s -> character.status = s);
            grid.addChild(statusInput);
        } else {
            grid.addChild(new Label(character.status));
        }

        if (editable || viewingAsGM) {
            grid.addChild(new Label(L10n.tr("more.gui.status.status.intoxication")));
            final Widget intoxicationWidget = getIntoxicationWidget();
            grid.addChild(intoxicationWidget);

            grid.addChild(new Label(L10n.tr("more.gui.status.status.attribution")));

            final Widget attributionWidget = getAttributionWidget();
            grid.addChild(attributionWidget);
        } else {
            grid.addChild(new Label(L10n.tr("more.gui.status.status.intoxication")));
            int intoxAmount = 0;
            for (boolean b : character.intoxication) {
                intoxAmount += b ? 1 : 0;
            }
            grid.addChild(new Label(String.valueOf(intoxAmount)));

            grid.addChild(new Label(L10n.tr("more.gui.status.status.attribution")));
            int attribAmount = 0;
            for (boolean b : character.attribution) {
                attribAmount += b ? 1 : 0;
            }
            grid.addChild(new Label(String.valueOf(attribAmount)));
        }

        grid.addChild(new Label(L10n.tr("more.gui.status.status.limbs")));
        final Widget limbsWidget = new Widget();
        limbsWidget.setLayout(new GridLayout(0));
        final Widget labelsWidget = new Widget();
        labelsWidget.setLayout(new ListLayout(ListLayout.Direction.HORIZONTAL, 5));
        final Widget boxesWidget = new Widget();
        boxesWidget.setLayout(new ListLayout(ListLayout.Direction.HORIZONTAL, 8));

        for (Map.Entry<Limb, Integer> e : character.limbs.entrySet()) {
            labelsWidget.addChild(new Label(e.getKey().tr()));
            final Widget limbWidget = getLimbWidget(e);

            boxesWidget.addChild(limbWidget);
        }

        limbsWidget.addChild(labelsWidget);
        limbsWidget.addChild(new Widget());
        limbsWidget.addChild(boxesWidget);
        grid.addChild(limbsWidget);


        if (entity != Minecraft.getMinecraft().thePlayer)
            fillEnemy();

//        if (editable)
//            fillEditable();
//        else
//            fillNonEditable();
    }

    private Widget getLimbWidget(Map.Entry<Limb, Integer> e) {
        final Widget limbWidget = new Widget();
        limbWidget.setLayout(new GridLayout(1));

        int writtenLimbs = e.getValue();
        for (int i = 0; i < 2; i++) {
            boolean isActive = true;
            if (writtenLimbs < 2) {
                writtenLimbs += 1;
                isActive = false;
            }

            Crossbox crossbox = new Crossbox(isActive);
            if (!viewingAsGM && !isActive) {
                crossbox.setDisabled(true);
            }

            if (!viewingAsGM && !editable) {
                crossbox.setDisabled(true);
            }

            crossbox.setCallback(b -> {
                character.limbs.put(e.getKey(), b ? e.getValue() + 1 : e.getValue() - 1);
            });

            limbWidget.addChild(crossbox);
        }
        return limbWidget;
    }

    private Widget getAttributionWidget() {
        final Widget attributionWidget = new Widget();
        attributionWidget.setLayout(new ListLayout(ListLayout.Direction.HORIZONTAL, 3));
        for (int i = 0; i < character.attribution.size(); i++) {
            boolean isActive = character.attribution.get(i);
            final Checkbox checkbox = new Checkbox(isActive);

            int finalI = i;
            checkbox.setCallback(b -> {
                character.attribution.set(finalI, b);
            });
            attributionWidget.addChild(checkbox);
        }
        return attributionWidget;
    }

    private Widget getIntoxicationWidget() {
        final Widget intoxicationWidget = new Widget();
        intoxicationWidget.setLayout(new ListLayout(ListLayout.Direction.HORIZONTAL, 3));
        for (int i = 0; i < character.intoxication.size(); i++) {
            boolean isActive = character.intoxication.get(i);
            final Checkbox checkbox = new Checkbox(isActive);
            if (!viewingAsGM && isActive) {
                checkbox.setDisabled(true);
            }

            int finalI = i;
            checkbox.setCallback(b -> {
                character.intoxication.set(finalI, b);
            });

            intoxicationWidget.addChild(checkbox);
        }
        return intoxicationWidget;
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
