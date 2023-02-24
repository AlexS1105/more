package msifeed.mc.more.crabs.character;

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
        this.proficiency = nbt.getBoolean("name");
    }

    public int getBonus(Character character)
    {
        int bonus = character.abilities.get(this.stat);

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
