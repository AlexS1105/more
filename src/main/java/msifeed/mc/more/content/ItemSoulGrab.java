package msifeed.mc.more.content;

import msifeed.mc.Bootstrap;
import msifeed.mc.extensions.tweaks.HealthModifier;
import msifeed.mc.genesis.GenesisCreativeTab;
import msifeed.mc.more.More;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.more.crabs.utils.Differ;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class ItemSoulGrab extends Item {
    public static String ITEM_NAME = "soul_grab";

    ItemSoulGrab() {
        setUnlocalizedName(ITEM_NAME);
        setTextureName(Bootstrap.MODID + ":" + ITEM_NAME);
        setCreativeTab(GenesisCreativeTab.ITEMS);
        setMaxStackSize(16);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean extra) {
        lines.add(L10n.tr("item.soul_grab.desc"));
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer))
            return false;
        if (player.worldObj.isRemote)
            return true;

        final EntityPlayer target = (EntityPlayer) entity;
        final Character playerAfter = CharacterAttribute.require(player);
        final Character playerBefore = new Character(playerAfter);
        final ItemDefines config = More.DEFINES.get().items;

        if (playerAfter.estitence >= 100) {
            player.addChatMessage(new ChatComponentText(L10n.fmt("more.est.grab_not_enough", target.getDisplayName())));
            return true;
        }

        final Character targetAfter = CharacterAttribute.require(target);
        final Character targetBefore = new Character(targetAfter);

        if (targetAfter.estitence <= 20) {
            player.addChatMessage(new ChatComponentText(L10n.fmt("more.est.grab_too_much")));
            return true;
        }

        playerAfter.estitence = Math.min(playerAfter.estitence + config.soulGrabEstitence, 90);
        playerAfter.sin = Math.min(playerAfter.sin + config.soulGrabSin, 100);
        targetAfter.estitence = Math.max(targetAfter.estitence - config.soulGrabEstitence, 10);

        player.addChatMessage(new ChatComponentText(L10n.fmt("more.est.grab_from", target.getDisplayName())));
        target.addChatMessage(new ChatComponentText(L10n.fmt("more.est.grab_to", player.getDisplayName())));
        stack.stackSize--;

        CharacterAttribute.INSTANCE.set(player, playerAfter);
        Differ.printDiffs((EntityPlayerMP)player, player, playerBefore, playerAfter);
        if (playerBefore.countMaxHealth() != playerAfter.countMaxHealth())
            HealthModifier.applyModifier(player, playerAfter);

        CharacterAttribute.INSTANCE.set(target, targetAfter);
        Differ.printDiffs((EntityPlayerMP)target, target, targetBefore, targetAfter);
        if (targetBefore.countMaxHealth() != targetAfter.countMaxHealth())
            HealthModifier.applyModifier(target, targetAfter);

        return true;
    }
}
