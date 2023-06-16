package msifeed.mc.extensions.locks.items;

import msifeed.mc.Bootstrap;
import msifeed.mc.commons.logs.ExternalLogs;
import msifeed.mc.extensions.locks.LockObject;
import msifeed.mc.extensions.locks.LockType;
import msifeed.mc.more.crabs.character.Ability;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.rolls.Criticalness;
import msifeed.mc.more.crabs.rolls.Modifiers;
import msifeed.mc.more.crabs.rolls.Rolls;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.more.crabs.utils.MetaAttribute;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;

public class AccessTunerItem extends LockpickItem {
    public static final String ID = "lock_access_tuner";

    public AccessTunerItem() {
        setUnlocalizedName(ID);
        setTextureName(Bootstrap.MODID + ":" + ID);
    }

    @Override
    protected boolean canPick(LockObject lock) {
        return lock.getLockType() == LockType.DIGITAL;
    }

    @Override
    protected boolean rollPick(LockObject lock, ItemStack pick, EntityPlayer player) {
        final Character c = CharacterAttribute.require(player);
        final Modifiers m = MetaAttribute.require(player).modifiers;
        final Rolls.Result result = Rolls.rollAbility(c, m, Ability.INT);
        final String fmtResult = result.format(m.roll + m.metaRoll, m.toAbility(Ability.INT), Ability.INT);
        ExternalLogs.log(player, "roll", String.format("pick digital lock (diff %d) = %s", lock.getDifficulty(), fmtResult));

        if (result.beats(lock.getDifficulty()))
            return true;
        else {
            consumePick(lock, pick, player, result.crit == Criticalness.FAIL ? 0 : result.result);
            return false;
        }
    }

    @Override
    protected void makeBreakSound(LockObject lock) {
        final TileEntity te = lock.getTileEntity();
        te.getWorldObj().playSoundEffect(te.xCoord, te.yCoord, te.zCoord, "random.fizz", 0.3f, 3);
    }

    @Override
    protected void doPick(LockObject lock) {
        lock.setSecret(LockItem.DEFAULT_DIGITAL_SECRET);
    }

    @Override
    protected void successMessage(LockObject lock, EntityPlayer player) {
        final TileEntity te = lock.getTileEntity();
        player.addChatMessage(new ChatComponentTranslation("more.lock.hacked"));
        te.getWorldObj().playSoundEffect(te.xCoord, te.yCoord, te.zCoord, "random.orb", 0.3f, 99999f);
    }
}
