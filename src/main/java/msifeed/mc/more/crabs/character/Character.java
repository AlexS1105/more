package msifeed.mc.more.crabs.character;

import msifeed.mc.commons.traits.Trait;
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
    }

    public Set<Trait> traits() {
        return traits;
    }

    public boolean has(Trait trait) {
        return traits.contains(trait);
    }

    public int countMaxHealth() {
        return Math.max((estitence - 20) / 2, 1);
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
    }
}
