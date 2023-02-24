package msifeed.mc.more.crabs.character;

import msifeed.mc.more.crabs.rolls.Modifiers;
import net.minecraft.nbt.NBTTagCompound;

public class Skill {
    public String name;
    public int level;
    public Ability stat;
    public boolean proficiency;

    public Skill() {

    }

    public Skill(NBTTagCompound nbt) {
        this.name = nbt.getString("name");
        this.level = nbt.getInteger("level");
        this.stat = Ability.values()[nbt.getInteger("stat")];
        this.proficiency = nbt.getBoolean("proficiency");
    }

    public int getBonus(Character character, Modifiers modifiers) {
        return character.abilities.get(this.stat) + modifiers.toAbility(this.stat) + getMod(character);
    }

    public int getMod(Character character) {
        int bonus = 0;

        if (level >= 2) {
            bonus += character.soulCoefficient();
        }

        if (level == 1) {
            bonus += 1;
        } else if (level == 3) {
            bonus += 3;
        }

        return bonus;
    }

    public NBTTagCompound toNbt()
    {
        final NBTTagCompound nbt = new NBTTagCompound();

        nbt.setString("name", name);
        nbt.setInteger("level", level);
        nbt.setInteger("stat", stat.ordinal());
        nbt.setBoolean("proficiency", proficiency);

        return nbt;
    }
}
