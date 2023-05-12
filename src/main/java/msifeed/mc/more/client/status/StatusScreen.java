package msifeed.mc.more.client.status;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import msifeed.mc.mellow.mc.MellowGuiScreen;
import msifeed.mc.mellow.widgets.Widget;
import msifeed.mc.mellow.widgets.basic.Separator;
import msifeed.mc.mellow.widgets.button.ButtonLabel;
import msifeed.mc.mellow.widgets.tabs.TabArea;
import msifeed.mc.mellow.widgets.window.Window;
import msifeed.mc.more.crabs.character.CharRpc;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.sys.attributes.AttributeUpdateEvent;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;

public class StatusScreen extends MellowGuiScreen {
    private final EntityLivingBase entity;
    private final boolean editable;
    private final boolean isGm;
    private Character character;

    private final Widget content;

    public StatusScreen(EntityLivingBase entity, boolean editable, boolean isGm) {
        this.entity = entity;
        this.editable = editable;
        this.isGm = isGm;

        updateCharacter();

        if (character == null) {
            content = null;
            closeGui();
            return;
        }

        final Window window = new Window();
        window.setTitle(L10n.fmt("more.gui.status.title", character.name.isEmpty() ? entity.getCommandSenderName() : character.name));
        scene.addChild(window);
        content = window.getContent();

        refill();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void updateCharacter() {
        CharacterAttribute.get(entity).ifPresent(c -> character = new Character(c));
        if (character == null) {
            closeGui();
        }
    }

    private void refill() {
        updateCharacter();

        content.clearChildren();

        final TabArea tabs = new TabArea();
        final ParamsView paramsView = new ParamsView(entity, character, editable);
        final IllnessView illnessView = new IllnessView(character, isGm);
        final EditAbilitiesView abilitiesView = new EditAbilitiesView(character, false);
        final OtherView otherView = new OtherView(character, entity, editable);

        if (editable)
            tabs.addTab(L10n.tr("more.gui.status.status"), paramsView);
        if (editable || character.illness.limit > 0)
            tabs.addTab(L10n.tr("more.gui.status.illness"), illnessView);
        if (editable || isGm)
            tabs.addTab(L10n.tr("more.gui.status.abilities"), abilitiesView);
        tabs.addTab(L10n.tr("more.gui.status.other"), otherView);
        content.addChild(tabs);

        if (editable) {
            final ButtonLabel submitBtn = new ButtonLabel(L10n.tr("more.gui.apply"));
            submitBtn.setClickCallback(() -> {
                if (!entity.isEntityAlive()) {
                    System.out.println("entity is actually dead");
                    closeGui();
                } else if (character != null) {
                    CharacterAttribute.INSTANCE.set(entity, character);
                    CharRpc.updateChar(entity.getEntityId(), character);
                    paramsView.refill();
                    illnessView.refill();
                    abilitiesView.refill();
                    otherView.refill();
                }
            });
            content.addChild(new Separator());
            content.addChild(submitBtn);
        }
    }

    @Override
    public void closeGui() {
        MinecraftForge.EVENT_BUS.unregister(this);
        super.closeGui();
    }

    @SubscribeEvent
    public void onAttributeUpdate(AttributeUpdateEvent event) {
        if (event.entity == entity && event.attr instanceof CharacterAttribute) {
            updateCharacter();
        }
    }
}
