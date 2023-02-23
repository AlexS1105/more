package msifeed.mc.more.crabs.character;

import msifeed.mc.commons.defines.Defines;
import msifeed.mc.commons.traits.Trait;
import msifeed.mc.more.More;
import net.minecraft.nbt.NBTTagCompound;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Character {
    public String name = "";
    public String wikiPage = "";
    public Set<Trait> traits = new HashSet<>();

    public EnumMap<Ability, Integer> abilities = new EnumMap<>(Ability.class);
    public Illness illness = new Illness();

    public int fistsDamage = 0;
    public int armor = 0;
    public int damageThreshold = 0;

    public int estitence = 62;
    public int sin = 0;
    public boolean visibleOnMap = true;

    public transient boolean loadedFromCrust = false;

    public float healthMod = 0;

    public Character() {
        System.out.println("CRUST init");
        for (Ability f : Ability.values())
            abilities.put(f, 7);
    }

    public Character(Character c) {
        name = c.name;
        wikiPage = c.wikiPage;
        traits.addAll(c.traits);
        for (EnumMap.Entry<Ability, Integer> e : c.abilities.entrySet())
            abilities.put(e.getKey(), e.getValue());
        illness.unpack(c.illness.pack());
        fistsDamage = c.fistsDamage;
        armor = c.armor;
        damageThreshold = c.damageThreshold;
        estitence = c.estitence;
        sin = c.sin;
        visibleOnMap = c.visibleOnMap;
        loadedFromCrust = c.loadedFromCrust;
        healthMod = c.healthMod;
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
        return sin > 0 ? 1 : sin;
    }

    public NBTTagCompound toNBT() {
        final NBTTagCompound c = new NBTTagCompound();

        c.setString(Tags.name, name);
        c.setString(Tags.wiki, wikiPage);
        c.setIntArray(Tags.traits, traits.stream().filter(Objects::nonNull).mapToInt(t -> t.code).toArray());

        final int[] abilitiesArr = new int[Ability.values().length];
        for (Ability f : Ability.values())
            abilitiesArr[f.ordinal()] = abilities.getOrDefault(f, 0);
        c.setIntArray(Tags.abilities, abilitiesArr);

        c.setInteger(Tags.illness, illness.pack());

        c.setInteger(Tags.fistsDmg, fistsDamage);
        c.setInteger(Tags.armor, armor);
        c.setInteger(Tags.damageThreshold, damageThreshold);

        c.setInteger(Tags.estitence, estitence);
        c.setInteger(Tags.sin, sin);

        c.setBoolean(Tags.visibleOnMap, visibleOnMap);

        c.setFloat(Tags.healthMod, healthMod);

        c.setBoolean(Tags.loadedFromCrust, loadedFromCrust);

        return c;
    }

    public void fromNBT(NBTTagCompound c) {
        name = c.getString(Tags.name);
        wikiPage = c.getString(Tags.wiki);
        traits = Trait.decode(c.getIntArray(Tags.traits));

        final int[] abilitiesArr = c.getIntArray(Tags.abilities);
        for (Ability f : Ability.values())
            abilities.put(f, abilitiesArr[f.ordinal()]);

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
        loadedFromCrust = c.getBoolean(Tags.loadedFromCrust);
    }

    private static class Tags {
        static final String name = "name";
        static final String wiki = "wiki";
        static final String traits = "traits";
        static final String abilities = "abs";
        static final String illness = "illness";
        static final String fistsDmg = "fistsDmg";
        static final String armor = "armor";
        static final String damageThreshold = "dmgThr";
        static final String estitence = "estitence";
        static final String sin = "sin";
        static final String visibleOnMap = "visibleOnMap";
        static final String healthMod = "healthMod";
        static final String loadedFromCrust = "loadedFromCrust";
    }
}
