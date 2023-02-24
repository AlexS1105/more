package msifeed.mc.more.crabs.rolls;

import msifeed.mc.more.crabs.character.Ability;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.character.Skill;
import msifeed.mc.more.crabs.combat.CombatNotifications;

public final class Rolls {
    public static Result rollAbility(Character c, Modifiers m, Ability a) {
        final Result r = new Result();

        r.dice = Dices.n3d7m3();
        r.ability = c.abilities.get(a);
        r.flatMod = m.roll;
        r.statMod = m.toAbility(a);
        r.result = r.dice + r.ability + r.statMod + r.flatMod;

        return r;
    }

    public static Result rollSkill(Character c, Modifiers m, Skill s) {
        final Result r = new Result();

        r.dice = Dices.n3d7m3();
        r.ability = c.abilities.get(s.stat);
        r.flatMod = m.roll;
        r.statMod = m.toAbility(s.stat);
        r.skillMod = s.getMod(c);
        r.result = r.dice + r.ability + r.statMod + r.flatMod + r.skillMod;

        return r;
    }

    public static class Result {
        public Criticalness crit = Dices.critical();
        public int dice;
        public int ability;
        public int flatMod;
        public int statMod;
        public int result;
        public int skillMod;

        public boolean beats(int threshold) {
            switch (crit) {
                case FAIL:
                    return false;
                case LUCK:
                    return true;
                default:
                    return result >= threshold;
            }
        }

        public String format(int rollMod, int abilityMod, Ability a) {
            final StringBuilder sb = new StringBuilder();

            if (crit == Criticalness.FAIL)
                sb.append("FAIL ");
            else if (crit == Criticalness.LUCK)
                sb.append("LUCK ");

            sb.append("[");
            sb.append(result);
            sb.append(']');

            if (rollMod != 0 || abilityMod != 0) {
                sb.append(" (");
                if (rollMod != 0)
                    sb.append(CombatNotifications.explicitSignInt(rollMod));
                if (abilityMod != 0) {
                    if (rollMod != 0)
                        sb.append(' ');
                    sb.append(a.trShort());
                    sb.append(CombatNotifications.explicitSignInt(abilityMod));
                }
                sb.append(')');
            }

            return sb.toString();
        }
    }
}
