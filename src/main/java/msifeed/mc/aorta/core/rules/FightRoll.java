package msifeed.mc.aorta.core.rules;

import msifeed.mc.aorta.Aorta;
import msifeed.mc.aorta.core.character.Character;
import msifeed.mc.aorta.core.character.Feature;
import msifeed.mc.aorta.core.status.CharStatus;

import java.util.List;
import java.util.stream.Stream;

public class FightRoll extends Roll {
    public FightAction action;

    public FightRoll(Character character, CharStatus status, FightAction action, int mod) {
        final List<Double> featMods = Aorta.DEFINES.rules().modifiers.get(action);
        final double featSum = Stream.of(Feature.values())
                .mapToDouble(f -> character.features.get(f).roll() * featMods.get(f.ordinal()))
                .sum();
        final int sanity = SanityMod.calc(status);

        this.action = action;
        this.roll = (int) Math.floor(featSum);
        this.mod = mod;
        this.sanity = sanity;
        this.result = roll + mod + sanity;
        this.critical = Critical.roll();
    }
}
