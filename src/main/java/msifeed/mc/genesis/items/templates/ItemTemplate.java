package msifeed.mc.genesis.items.templates;

import com.google.common.collect.Multimap;
import msifeed.mc.commons.logs.ExternalLogs;
import msifeed.mc.extensions.chat.SpeechatRpc;
import msifeed.mc.extensions.chat.formatter.MiscFormatter;
import msifeed.mc.genesis.GenesisTrait;
import msifeed.mc.genesis.items.IItemTemplate;
import msifeed.mc.genesis.items.ItemCommons;
import msifeed.mc.genesis.items.ItemGenesisUnit;
import msifeed.mc.more.More;
import msifeed.mc.more.crabs.action.Action;
import msifeed.mc.more.crabs.action.ActionRegistry;
import msifeed.mc.more.crabs.combat.CombatContext;
import msifeed.mc.more.crabs.combat.CombatManager;
import msifeed.mc.more.crabs.combat.CombatNotifications;
import msifeed.mc.more.crabs.utils.CombatAttribute;
import msifeed.mc.sys.utils.ChatUtils;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;

import java.util.List;
import java.util.Objects;

public class ItemTemplate extends Item implements IItemTemplate {
    private final ItemGenesisUnit unit;

    public ItemTemplate(ItemGenesisUnit unit) {
        this.unit = unit;
        setUnlocalizedName(unit.id);
        this.setMaxDamage(unit.durData.maxDurability);
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemStack) {
        final String name = unit.title != null
                ? unit.title
                : super.getItemStackDisplayName(itemStack);
        return unit.rarity.color.toString() + name;
    }

    @Override
    public void addInformation(ItemStack itemStack, EntityPlayer player, List lines, boolean debug) {
        ItemCommons.addInformation(unit, itemStack, lines);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemStack) {
        return unit.hasTrait(GenesisTrait.action_bow) ? EnumAction.bow : EnumAction.none;
    }

    @Override
    public int getMaxDamage(ItemStack itemStack) {
        return unit.durData.maxDurability != 0 ? unit.durData.maxDurability : -1;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemStack) {
        if (unit.maxUsages > 0 || unit.hasTrait(GenesisTrait.reusable))
            return 32;
        else if (unit.hasTrait(GenesisTrait.action_bow))
            return 72000;
        else
            return 0;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return 1 - ((unit.durData.maxDurability - (double)itemStack.getItemDamage()) / unit.durData.maxDurability);
    }

    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (unit.durData.maxDurability > 0 && itemStack.getItemDamage() >= unit.durData.maxDurability) {
            if (world.isRemote)
                player.addChatMessage(new ChatComponentText("§4" + L10n.fmt("more.gen.broken")));
            return itemStack;
        }

        final int duration = getMaxItemUseDuration(itemStack);
        if (duration > 0)
            player.setItemInUse(itemStack, duration);
        return itemStack;
    }

    private String getUseText(EntityPlayer player, ItemStack itemStack, boolean special) {
        if (unit.hasTrait(GenesisTrait.reusable))
            if (unit.maxUsages == 0)
                return special ? "more.gen.attack_special" : "more.gen.attack";
            else
                if (itemStack.getTagCompound().getInteger("usages") == unit.maxUsages)
                    return "more.gen.reload";
                else
                    return special ? "more.gen.shot_special" : "more.gen.shot";

        return "more.gen.used";
    }

    private void onUse(EntityPlayer player, ItemStack itemStack, boolean special) {
        if (unit.crabsData.action != null) {
            if (!player.worldObj.isRemote) {
                final CombatContext com = CombatAttribute.require(player);
                final Action newAction = ActionRegistry.getFullAction(unit.crabsData.action);
                if (CombatManager.INSTANCE.doAction(player, com, newAction))
                    CombatNotifications.actionChanged(player, newAction);
            }
        }
    }

    private void onReload(EntityPlayer player, ItemStack itemStack, boolean special) {

    }

    @Override
    public ItemStack onEaten(ItemStack itemStack, World world, EntityPlayer player) {
        if (unit.maxUsages > 0 || unit.hasTrait(GenesisTrait.reusable)) {
            if (!itemStack.hasTagCompound()) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setInteger("usages", unit.maxUsages);
                itemStack.setTagCompound(compound);
            }

            final int usages = itemStack.getTagCompound().getInteger("usages");
            final boolean special = player.isSneaking()
                    && (unit.specialAttackCost > 0 && usages >= unit.specialAttackCost || unit.maxUsages == 0);
            final int cost = special ? unit.specialAttackCost : 1;

            if (!world.isRemote && usages > cost
                    || unit.hasTrait(GenesisTrait.reusable) && usages != 0 && unit.durData.maxDurability > 0) {
                final int damage = special ? unit.durData.getNextSpecialDamage() : unit.durData.getNextDamage();
                final int durability = Math.max(1, itemStack.getItemDamage() - damage);
                itemStack.setItemDamage(durability);
            }

            if (usages > cost) {
                itemStack.getTagCompound().setInteger("usages", usages - cost);

                if (!world.isRemote)
                    onUse(player, itemStack, special);
            } else {
                if (unit.hasTrait(GenesisTrait.reusable)) {
                    if (unit.maxUsages > 0) {
                        if (!world.isRemote)
                            if (usages != 0)
                                onUse(player, itemStack, special);
                            else
                                onReload(player, itemStack, special);

                        itemStack.getTagCompound().setInteger("usages", usages == 0 ? unit.maxUsages : 0);
                    } else {
                        if (!world.isRemote)
                            onUse(player, itemStack, special);
                    }
                } else {
                    if (!world.isRemote)
                        onUse(player, itemStack, special);
                    itemStack.stackSize--;

                    if (itemStack.stackSize > 0)
                        itemStack.getTagCompound().setInteger("usages", unit.maxUsages);
                }
            }
            if (!world.isRemote && player instanceof EntityPlayerMP) {
                final String name = ChatUtils.getPrettyName(player);
                final String text = L10n.fmt(getUseText(player, itemStack, special), itemStack.getDisplayName());
                SpeechatRpc.sendRaw(player, More.DEFINES.get().chat.logRadius, MiscFormatter.formatLog(name, text));
                ExternalLogs.log(player, "log", text);

                if (unit.maxUsages > 0 && itemStack.getTagCompound().getInteger("usages") == 0)
                    SpeechatRpc.sendRawTo((EntityPlayerMP) player, MiscFormatter.formatLog(name, L10n.tr("more.gen.needs_reload")));
            }
        }
        return itemStack;
    }

    @Override
    public ItemGenesisUnit getUnit() {
        return unit;
    }

    @Override
    public Multimap getItemAttributeModifiers()
    {
        Multimap multimap = super.getItemAttributeModifiers();

        if (unit.attackDamage != 0) {
            multimap.put(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName(), new AttributeModifier(field_111210_e, "Weapon modifier", unit.attackDamage, 0));
        }

        return multimap;
    }
    @Override
    public boolean getIsRepairable(ItemStack itemStack, ItemStack repairItem)
    {
        if (unit.repairItem != null) {
            final Item item = repairItem.getItem();
            final String registryName = Item.itemRegistry.getNameForObject(item);

            if (Objects.equals(registryName, unit.repairItem)) {
                return true;
            }
        }

        return super.getIsRepairable(itemStack, repairItem);
    }

    @Override
    public boolean hitEntity(ItemStack itemStack, EntityLivingBase entity, EntityLivingBase target)
    {
        itemStack.damageItem(1, target);
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemStack, World world, Block block, int x, int y, int z, EntityLivingBase player)
    {
        if ((double)block.getBlockHardness(world, x, y, z) != 0.0D)
        {
            itemStack.damageItem(1, player);
        }

        return true;
    }
}
