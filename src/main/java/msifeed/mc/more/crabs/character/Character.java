package msifeed.mc.more.crabs.character;

import com.google.common.collect.Lists;
import msifeed.mc.commons.traits.Trait;
import msifeed.mc.more.More;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.ArrayUtils;
import scala.Int;

import java.util.*;
import java.util.stream.Collectors;

import static msifeed.mc.sys.utils.NBTUtils.packBooleanList;
import static msifeed.mc.sys.utils.NBTUtils.unpackBooleanList;

public class Character {
    public String name = "";
    public String wikiPage = "";
    public String status = "";
    public Set<Trait> traits = new HashSet<>();

    public EnumMap<Ability, Integer> abilities = new EnumMap<>(Ability.class);
    public Illness illness = new Illness();
    public EnumMap<Trauma, Integer> traumas = new EnumMap<>(Trauma.class);

    public List<Boolean> intoxication = Lists.newArrayList(false, false);
    public List<Boolean> attribution = Lists.newArrayList(false, false);
    public EnumMap<Limb, Integer> limbs = new EnumMap<>(Limb.class);

    public int fistsDamage = 0;
    public int armor = 0;
    public int damageThreshold = 0;

    public int estitence = 62;
    public int sin = 0;
    public boolean visibleOnMap = true;

    public transient boolean loadedFromCrust = false;

    public float healthMod = 0;

    public List<Skill> skills = new ArrayList<>();

    public boolean isNpc = false;

    public Character() {
        for (Ability f : Ability.values())
            abilities.put(f, 7);
        for (Trauma t : Trauma.values())
            traumas.put(t, 0);
        for (Limb l : Limb.values())
            limbs.put(l, 2);
    }

    public Character(Character c) {
        name = c.name;
        wikiPage = c.wikiPage;
        status = c.status;
        traits.addAll(c.traits);
        skills.clear();
        skills.addAll(c.skills);
        abilities.putAll(c.abilities);
        traumas.putAll(c.traumas);
        limbs.putAll(c.limbs);
        illness.unpack(c.illness.pack());
        intoxication.clear();
        intoxication.addAll(c.intoxication);
        attribution.clear();
        attribution.addAll(c.attribution);
        fistsDamage = c.fistsDamage;
        armor = c.armor;
        damageThreshold = c.damageThreshold;
        estitence = c.estitence;
        sin = c.sin;
        visibleOnMap = c.visibleOnMap;
        loadedFromCrust = c.loadedFromCrust;
        healthMod = c.healthMod;
        isNpc = c.isNpc;
    }

    public Set<Trait> traits() {
        return traits;
    }

    public boolean has(Trait trait) {
        return traits.contains(trait);
    }

    public float countMaxHealth() {
        final StatDefines config = More.DEFINES.stats();
        final float enduranceHealthMod = abilities.get(Ability.END) * config.healthPerEndurance;
        final float determinationHealthMod = abilities.get(Ability.DET) * config.healthPerDetermination;
        return Math.max(1, (estitence - 20) * 0.5f
                + enduranceHealthMod
                + determinationHealthMod
                + healthMod);
    }

    public int sinLevel() {
        return sin;
    }

    public int soulCoefficient() {
        return Math.round(estitence / 10.0f);
    }

    public NBTTagCompound toNBT() {
        final NBTTagCompound c = new NBTTagCompound();

        c.setString(Tags.name, name);
        c.setString(Tags.wiki, wikiPage);
        c.setString(Tags.status, status);
        c.setIntArray(Tags.traits, traits.stream().filter(Objects::nonNull).mapToInt(t -> t.code).toArray());

        final int[] abilitiesArr = new int[Ability.values().length];
        for (Ability f : Ability.values())
            abilitiesArr[f.ordinal()] = abilities.getOrDefault(f, 0);
        c.setIntArray(Tags.abilities, abilitiesArr);

        final int[] traumasArr = new int[Trauma.values().length];
        for (Trauma t : Trauma.values()) {
            traumasArr[t.ordinal()] = traumas.getOrDefault(t, 0);
        }
        c.setIntArray(Tags.traumas, traumasArr);

        final int[] limbsArr = new int[Limb.values().length];
        for (Limb l : Limb.values()) {
            limbsArr[l.ordinal()] = limbs.getOrDefault(l, 2);
        }
        c.setIntArray(Tags.limbs, limbsArr);

        c.setByteArray(Tags.intoxication, packBooleanList(intoxication));
        c.setByteArray(Tags.attribution, packBooleanList(attribution));

        c.setInteger(Tags.illness, illness.pack());

        c.setInteger(Tags.fistsDmg, fistsDamage);
        c.setInteger(Tags.armor, armor);
        c.setInteger(Tags.damageThreshold, damageThreshold);

        c.setInteger(Tags.estitence, estitence);
        c.setInteger(Tags.sin, sin);

        c.setBoolean(Tags.visibleOnMap, visibleOnMap);

        c.setFloat(Tags.healthMod, healthMod);

        final NBTTagList skills = new NBTTagList();

        for (Skill skill : this.skills) {
            if (skill == null) continue;

            skills.appendTag(skill.toNbt());
        }

        c.setTag(Tags.skills, skills);
        c.setBoolean(Tags.isNpc, isNpc);

        return c;
    }

    public void fromNBT(NBTTagCompound c) {
        name = c.getString(Tags.name);
        wikiPage = c.getString(Tags.wiki);
        status = c.getString(Tags.status);
        traits = Trait.decode(c.getIntArray(Tags.traits));

        final int[] abilitiesArr = c.getIntArray(Tags.abilities);
        for (Ability f : Ability.values()) {
            abilities.put(f, abilitiesArr[f.ordinal()]);
        }

        final int[] traumasArr = c.getIntArray(Tags.traumas);
        for (Trauma t : Trauma.values()) {
            traumas.put(t, traumasArr[t.ordinal()]);
        }

        final int[] limbsArr = c.getIntArray(Tags.limbs);
        for (Limb l : Limb.values()) {
            limbs.put(l, limbsArr[l.ordinal()]);
        }

        intoxication = unpackBooleanList(c.getByteArray(Tags.intoxication));
        attribution = unpackBooleanList(c.getByteArray(Tags.attribution));

        illness.unpack(c.getInteger(Tags.illness));

        fistsDamage = c.getInteger(Tags.fistsDmg);
        armor = c.getInteger(Tags.armor);
        damageThreshold = c.getInteger(Tags.damageThreshold);

        estitence = c.getInteger(Tags.estitence);
        sin = c.getInteger(Tags.sin);

        if (c.hasKey(Tags.visibleOnMap))
            visibleOnMap = c.getBoolean(Tags.visibleOnMap);
        else
            visibleOnMap = true;

        healthMod = c.getFloat(Tags.healthMod);

        final NBTTagList skillsList = c.getTagList(Tags.skills, 10);

        skills.clear();
        for (int i = 0; i < skillsList.tagCount(); i++) {
            skills.add(new Skill(skillsList.getCompoundTagAt(i)));
        }

        isNpc = c.getBoolean(Tags.isNpc);
    }

    private static class Tags {
        static final String name = "name";
        static final String wiki = "wiki";
        static final String status = "status";
        static final String traits = "traits";
        static final String traumas = "traumas";
        static final String limbs = "limbs";
        static final String intoxication = "intoxication";
        static final String attribution = "attribution";
        static final String abilities = "abs";
        static final String illness = "illness";
        static final String fistsDmg = "fistsDmg";
        static final String armor = "armor";
        static final String damageThreshold = "dmgThr";
        static final String estitence = "estitence";
        static final String sin = "sin";
        static final String visibleOnMap = "visibleOnMap";
        static final String healthMod = "healthMod";
        static final String skills = "skills";
        static final String isNpc = "isNpc";
    }
}
