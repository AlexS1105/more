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

public class MagicalLockpickItem extends LockpickItem {
    public static final String ID = "lock_magical_lockpick";

    public MagicalLockpickItem() {
        setUnlocalizedName(ID);
        setTextureName(Bootstrap.MODID + ":" + ID);
    }

    @Override
    protected boolean canPick(LockObject lock) {
        return lock.getLockType() == LockType.MAGICAL;
    }

    protected boolean rollPick(LockObject lock, ItemStack pick, EntityPlayer player) {
        if (lock.getDifficulty() >= 100)
            return false;

        final Character c = CharacterAttribute.require(player);
        final Modifiers m = MetaAttribute.require(player).modifiers;
        final Rolls.Result result = Rolls.rollAbility(c, m, Ability.SPR);
        final String fmtResult = result.format(m.roll + m.metaRoll, m.toAbility(Ability.SPR), Ability.SPR);
        ExternalLogs.log(player, "roll", String.format("pick magical lock (diff %d) = %s", lock.getDifficulty(), fmtResult));

        if (result.beats(lock.getDifficulty()))
            return true;
        else {
            consumePick(lock, pick, player, result.crit == Criticalness.FAIL ? 0 : result.result);
            return false;
        }
    }
}
