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

class ItemGifterOffering extends Item {
    public static String ITEM_NAME = "gifter_offering";

    ItemGifterOffering() {
        setUnlocalizedName(ITEM_NAME);
        setTextureName(Bootstrap.MODID + ":" + ITEM_NAME);
        setCreativeTab(GenesisCreativeTab.ITEMS);
        setMaxStackSize(16);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean extra) {
        lines.add(L10n.tr("item.gifter_offering.desc"));
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

        if (playerAfter.estitence <= 20) {
            player.addChatMessage(new ChatComponentText(L10n.fmt("more.est.gift_not_enough")));
            return true;
        }

        final Character targetAfter = CharacterAttribute.require(target);
        final Character targetBefore = new Character(targetAfter);

        if (targetAfter.estitence >= 100) {
            player.addChatMessage(new ChatComponentText(L10n.fmt("more.est.gift_too_much", target.getDisplayName())));
            return true;
        }

        playerAfter.estitence = Math.max(playerAfter.estitence - config.gifterOfferingEstitence, 10);
        targetAfter.estitence = Math.min(targetAfter.estitence + config.gifterOfferingEstitence, 90);
        targetAfter.illness.illness = 0;
        targetAfter.illness.limit = 0;
        targetAfter.illness.treatment = 0;
        target.heal(target.getMaxHealth());

        player.addChatMessage(new ChatComponentText(L10n.fmt("more.est.gift_to", target.getDisplayName())));
        target.addChatMessage(new ChatComponentText(L10n.fmt("more.est.gift_from", player.getDisplayName())));
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
