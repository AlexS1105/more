package msifeed.mc.extensions.tweaks;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import java.util.UUID;

public class HealthModifier {
    private static final UUID MOD_UIID = UUID.fromString("713fc405-dcaa-4042-8f85-8929e6739ee9");

    public static void preInit() {
        MinecraftForge.EVENT_BUS.register(new HealthModifier());
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.world.isRemote || !(event.entity instanceof EntityPlayer))
            return;

        final EntityPlayer player = (EntityPlayer) event.entity;
        final Character c = CharacterAttribute.require(player);
        applyModifier(player, c);
    }

    public static void applyModifier(EntityPlayer player, Character c) {
        final IAttributeInstance attr = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        final AttributeModifier currMod = attr.getModifier(MOD_UIID);
        final AttributeModifier newMod =
                new AttributeModifier(MOD_UIID, "More", c.countMaxHealth() - 20, 0);

        if (currMod == null) {
            attr.applyModifier(newMod);
        } else if (currMod.getAmount() != newMod.getAmount()) {
            attr.removeModifier(currMod);
            attr.applyModifier(newMod);
        }
    }
}
