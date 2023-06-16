package msifeed.mc.more.client.combat;

import msifeed.mc.mellow.layout.ListLayout;
import msifeed.mc.mellow.mc.MellowGuiScreen;
import msifeed.mc.mellow.utils.SizePolicy;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.basic.Separator;
import msifeed.mc.mellow.widgets.button.ButtonLabel;
import msifeed.mc.mellow.widgets.scroll.ScrollArea;
import msifeed.mc.mellow.widgets.window.Window;
import msifeed.mc.more.crabs.action.Action;
import msifeed.mc.more.crabs.action.ActionHeader;
import msifeed.mc.more.crabs.action.ActionRegistry;
import msifeed.mc.more.crabs.action.effects.Effect;
import msifeed.mc.more.crabs.combat.CombatContext;
import msifeed.mc.more.crabs.combat.FighterInfo;
import msifeed.mc.more.crabs.rolls.Criticalness;
import msifeed.mc.more.crabs.rolls.Dices;
import msifeed.mc.more.crabs.utils.ActionAttribute;
import msifeed.mc.more.crabs.utils.CombatAttribute;
import msifeed.mc.sys.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class TestCombatScreen extends MellowGuiScreen {
    final ScrollArea scroll = new ScrollArea();

    public TestCombatScreen() {

        final Window window = new Window();
        window.setTitle("Test combat tool");
        scene.addChild(window);

        final Widget content = window.getContent();
        content.setLayout(ListLayout.VERTICAL);

        scroll.setSizeHint(150, 150);
        scroll.setSizePolicy(SizePolicy.Policy.MINIMUM, SizePolicy.Policy.MAXIMUM);
        content.addChild(scroll);

        refill();

        content.addChild(new Separator());

        final ButtonLabel reloadConfigBtn = new ButtonLabel("Reload config");
        reloadConfigBtn.setClickCallback(() -> {
            ConfigManager.reload();
            refill();
        });
        content.addChild(reloadConfigBtn);
    }

    private void refill() {
        scroll.clearChildren();
        ActionRegistry.getFullActions().stream()
                .sorted(ActionHeader::compareTo)
                .forEach(action -> {
                    final ButtonLabel btn = new ButtonLabel(action.getTitle());
                    btn.setClickCallback(() -> doAction(action));
                    scroll.addChild(btn);
                });
    }

    private static void doAction(Action action) {
        final EntityPlayer player = Minecraft.getMinecraft().thePlayer;

        CombatAttribute.INSTANCE.update(player, com -> {
            com.role = action.isOffencive() ? CombatContext.Role.OFFENCE : CombatContext.Role.DEFENCE;
            com.action = action;
        });
        ActionAttribute.INSTANCE.update(player, act -> act.action = action);
        final FighterInfo self = new FighterInfo(player);

        // Apply scores
        self.act.scorePlayerMod = self.mod.roll + self.mod.metaRoll;
        self.act.critical = Dices.critical();
        if (self.act.critical == Criticalness.FAIL)
            self.act.successful = false;

        send(player, String.format("%s - mod %d, crit: %s", action.getTitle(), self.act.scorePlayerMod, self.act.critical.toString()));

        for (Effect e : self.act.action.self) {
            if (e.shouldApply(Effect.Stage.PRE_SCORE, self, null)) {
                final int scoreBefore = self.act.scoreAction;
                e.apply(Effect.Stage.PRE_SCORE, self, null);
                final int scoreDiff = self.act.scoreAction - scoreBefore;
                send(player, String.format("  %s -> %d (%+d)", e.encode(), self.act.scoreAction, scoreDiff));
            }
        }

        for (Effect e : self.act.action.self) {
            if (e.shouldApply(Effect.Stage.SCORE, self, null)) {
                final int scoreBefore = self.act.scoreAction;
                e.apply(Effect.Stage.SCORE, self, null);
                final int scoreDiff = self.act.scoreAction - scoreBefore;
                send(player, String.format("  %s -> %d (%+d)", e.encode(), self.act.scoreAction, scoreDiff));
            }
        }

        send(player, String.format("result score %d", self.act.score()));
    }

    private static void send(EntityPlayer player, String text) {
        player.addChatMessage(new ChatComponentText(text));
    }
}
