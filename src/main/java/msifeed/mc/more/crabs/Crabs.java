package msifeed.mc.more.crabs;

import msifeed.mc.more.More;
import msifeed.mc.more.crabs.action.ActionRegistry;
import msifeed.mc.more.crabs.character.CharRpc;
import msifeed.mc.more.crabs.combat.CombatManager;
import msifeed.mc.more.crabs.combat.CombatRpc;
import msifeed.mc.more.crabs.combat.PotionsHandler;
import msifeed.mc.more.crabs.meta.MetaRpc;
import msifeed.mc.more.crabs.tags.Tags;
import msifeed.mc.more.crabs.utils.ActionAttribute;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.more.crabs.utils.CombatAttribute;
import msifeed.mc.more.crabs.utils.MetaAttribute;
import msifeed.mc.sys.attributes.AttributeHandler;

public class Crabs {
    public void preInit() {
        ActionRegistry.INSTANCE.init();
        CombatManager.INSTANCE.init();
        PotionsHandler.INSTANCE.init();
        Tags.INSTANCE.init();

        AttributeHandler.registerAttribute(CharacterAttribute.INSTANCE);
        AttributeHandler.registerAttribute(MetaAttribute.INSTANCE);
        AttributeHandler.registerAttribute(CombatAttribute.INSTANCE);
        AttributeHandler.registerAttribute(ActionAttribute.INSTANCE);

        More.RPC.register(CharRpc.INSTANCE);
        More.RPC.register(MetaRpc.INSTANCE);
        More.RPC.register(CombatRpc.INSTANCE);
    }
}
