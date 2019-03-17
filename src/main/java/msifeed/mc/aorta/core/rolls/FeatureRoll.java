package msifeed.mc.aorta.core.rolls;

import msifeed.mc.aorta.core.character.CharStatus;
import msifeed.mc.aorta.core.character.Character;
import msifeed.mc.aorta.core.character.Feature;

import java.util.stream.Stream;

public class FeatureRoll extends Roll {
    public Feature[] features;

    public FeatureRoll(Character character, CharStatus status, Feature... feats) {
        final double rollAvg = Stream.of(feats)
                .map(f -> character.features.get(f) + status.modifiers.feat(f))
                .mapToDouble(Dices::feature)
                .sum() / feats.length;

        this.features = feats;
        this.mods = status.modifiers;
        this.sanity = sanityMod(status.sanity);
        this.roll = Dices.randRound(rollAvg);
        this.result = roll + mods.rollMod + sanity;
        this.critical = Critical.roll();
    }

    public boolean check(int difficulty) {
        return critical == Critical.LUCK || critical != Critical.FAIL && result >= difficulty;
    }
}
