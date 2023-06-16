package msifeed.mc.more.crabs.meta;

import msifeed.mc.more.crabs.character.Ability;
import msifeed.mc.more.crabs.character.Skill;
import msifeed.mc.more.crabs.rolls.Modifiers;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MetaInfo {
    public Modifiers modifiers = new Modifiers();
    public boolean receiveGlobal = true;

    public MetaInfo() {
    }

    public MetaInfo(MetaInfo m) {
        this.modifiers = new Modifiers(m.modifiers);
        this.receiveGlobal = m.receiveGlobal;
    }

    public NBTTagCompound toNBT() {
        final NBTTagCompound c = new NBTTagCompound();

        c.setInteger("dmod", modifiers.damage);
        c.setInteger("rmod", modifiers.roll);
        c.setInteger("mrmod", modifiers.metaRoll);

        final Ability[] feats = Ability.values();
        final int[] featsArray = new int[feats.length];
        final int[] traumasArray = new int[feats.length];
        for (int i = 0; i < feats.length; i++) {
            featsArray[i] = modifiers.customToAbility(feats[i]);
            traumasArray[i] = modifiers.metaToAbility(feats[i]);
        }
        c.setIntArray("fmod", featsArray);
        c.setIntArray("tmod", traumasArray);

        NBTTagList skillList = new NBTTagList();
        for (Skill disabledSkill : modifiers.disabledSkills) {
            skillList.appendTag(disabledSkill.toNbt());
        }
        c.setTag("disabled_skills", skillList);

        c.setBoolean("recglob", receiveGlobal);

        return c;
    }

    public void fromNBT(NBTTagCompound c) {
        modifiers.damage = c.getInteger("dmod");
        modifiers.roll = c.getInteger("rmod");
        modifiers.metaRoll = c.getInteger("mrmod");

        modifiers.abilities.clear();
        final Ability[] feats = Ability.values();
        final int[] featsArray = c.getIntArray("fmod");
        final int[] traumasArray = c.getIntArray("tmod");
        for (int i = 0; i < feats.length; i++) {
            modifiers.metaAbilities.put(feats[i], traumasArray[i]);
            modifiers.abilities.put(feats[i], featsArray[i]);
        }

        modifiers.disabledSkills.clear();
        NBTTagList skillList = c.getTagList("disabled_skills", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < skillList.tagCount(); i++) {
            NBTTagCompound skillNBT = skillList.getCompoundTagAt(i);
            modifiers.disabledSkills.add(new Skill(skillNBT));
        }

        receiveGlobal = c.getBoolean("recglob");
    }
}
