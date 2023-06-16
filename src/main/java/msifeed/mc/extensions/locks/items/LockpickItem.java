package msifeed.mc.extensions.locks.items;

import msifeed.mc.Bootstrap;
import msifeed.mc.commons.logs.ExternalLogs;
import msifeed.mc.extensions.locks.LockObject;
import msifeed.mc.extensions.locks.LockType;
import msifeed.mc.extensions.locks.Locks;
import msifeed.mc.more.More;
import msifeed.mc.more.crabs.character.Ability;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.rolls.Criticalness;
import msifeed.mc.more.crabs.rolls.Modifiers;
import msifeed.mc.more.crabs.rolls.Rolls;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.more.crabs.utils.MetaAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class LockpickItem extends Item {
    public static final String ID = "lock_lockpick";

    public LockpickItem() {
        setCreativeTab(Locks.LOCKS);
        setUnlocalizedName(ID);
        setTextureName(Bootstrap.MODID + ":" + ID);
    }

    protected boolean canPick(LockObject lock) {
        return lock.getLockType() == LockType.BUILD_IN || lock.getLockType() == LockType.PADLOCK;
    }

    protected boolean rollPick(LockObject lock, ItemStack pick, EntityPlayer player) {
        if (lock.getDifficulty() >= 100)
            return false;

        final Character c = CharacterAttribute.require(player);
        final Modifiers m = MetaAttribute.require(player).modifiers;
        final Rolls.Result result = Rolls.rollAbility(c, m, Ability.REF);
        final String fmtResult = result.format(m.roll + m.metaRoll, m.toAbility(Ability.REF), Ability.REF);
        ExternalLogs.log(player, "roll", String.format("pick mechanical lock (diff %d) = %s", lock.getDifficulty(), fmtResult));

        if (result.beats(lock.getDifficulty()))
            return true;
        else {
            consumePick(lock, pick, player, result.crit == Criticalness.FAIL ? 0 : result.result);
            return false;
        }
    }

    protected void consumePick(LockObject lock, ItemStack pick, EntityPlayer player, int roll) {
        if (roll <= lock.getDifficulty() - More.DEFINES.get().locks.pickBreakOffset) {
            pick.stackSize--;
            makeBreakSound(lock);
            player.addChatMessage(new ChatComponentTranslation("more.lock.pick_break"));
            ExternalLogs.log(player, "log", "pick broken, left " + pick.stackSize);
        }
    }

    protected void makeBreakSound(LockObject lock) {
        final TileEntity te = lock.getTileEntity();
        te.getWorldObj().playSoundEffect(te.xCoord, te.yCoord, te.zCoord, "random.break", 0.3f, 3);
    }

    protected void doPick(LockObject lock) {
        lock.setLocked(!lock.isLocked());
    }

    protected void successMessage(LockObject lock, EntityPlayer player) {
        if (lock.isLocked())
            player.addChatMessage(new ChatComponentTranslation("more.lock.locked"));
        else
            player.addChatMessage(new ChatComponentTranslation("more.lock.unlocked"));
    }

    private boolean tryToPick(LockObject lock, ItemStack pick, EntityPlayer player) {
        if (rollPick(lock, pick, player)) {
            doPick(lock);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
        final LockObject lock = LockObject.find(world, x, y, z);
        if (lock == null || !lock.hasLock())
            return false;

        if (world.isRemote)
            return false;

        if (canPick(lock) && tryToPick(lock, itemStack, player)) {
            successMessage(lock, player);
            ExternalLogs.log(player, "log", lock.isLocked() ? "[locked]" : "[unlocked]");
        }

        return true;
    }
}
