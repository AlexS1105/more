package msifeed.mc.more.client.combat;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import msifeed.mc.commons.traits.Trait;
import msifeed.mc.mellow.layout.ListLayout;
import msifeed.mc.mellow.mc.MellowGuiScreen;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.tabs.TabArea;
import msifeed.mc.mellow.widgets.window.Window;
import msifeed.mc.more.crabs.combat.CombatContext;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.more.crabs.utils.CombatAttribute;
import msifeed.mc.sys.attributes.AttributeUpdateEvent;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;

public class CombatScreen extends MellowGuiScreen {
    private final EntityLivingBase entity;
    private final Widget content;

    private final ActionsView actionsView;
    private final ProgressView progressView;
    private final ModsView modsView;
    private final RollAbilityView rollAbilityView;
    private final BuffView buffView;
    private final ShrimpsActionView shrimpsActionView;

    public CombatScreen(EntityLivingBase entity) {
        this.entity = entity;
        this.actionsView = new ActionsView(entity);
        this.progressView = new ProgressView(entity);
        this.modsView = new ModsView(entity);
        this.rollAbilityView = new RollAbilityView(entity);
        this.buffView = new BuffView(entity);
        this.shrimpsActionView = new ShrimpsActionView(entity);

        final Window window = new Window();
        window.setTitle(L10n.fmt("more.gui.combat.title", entity.getCommandSenderName()));
        scene.addChild(window);

        content = window.getContent();
        content.setLayout(new ListLayout(ListLayout.Direction.HORIZONTAL, 2));

        content.addChild(actionsView);

        final TabArea tabs = new TabArea();
        tabs.addTab(L10n.tr("more.gui.combat.progress"), progressView);
        tabs.addTab(L10n.tr("more.gui.combat.mods"), modsView);
        tabs.addTab(L10n.tr("more.gui.combat.rolls"), rollAbilityView);
        tabs.addTab(L10n.tr("more.gui.combat.shrimps"), shrimpsActionView);

        final EntityPlayer self = Minecraft.getMinecraft().thePlayer;
        if (CharacterAttribute.has(self, Trait.gm))
            tabs.addTab("Buffs", buffView);

        content.addChild(tabs);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void refill() {
        actionsView.refill();
        progressView.refill();
        modsView.refill();
        buffView.refill();
    }

    @Override
    public void closeGui() {
        MinecraftForge.EVENT_BUS.unregister(this);
        super.closeGui();
    }

    @SubscribeEvent
    public void onAttributeUpdate(AttributeUpdateEvent event) {
        if (event.attr instanceof CombatAttribute) {
            if (event.entity != entity && event.entity != Minecraft.getMinecraft().thePlayer)
                return;

            final boolean hasLeft = CombatAttribute.get((EntityLivingBase) event.entity)
                    .map(context -> context.phase == CombatContext.Phase.LEAVE)
                    .orElse(false);
            if (hasLeft)
                closeGui();
            else
                refill();
        }
    }
}
