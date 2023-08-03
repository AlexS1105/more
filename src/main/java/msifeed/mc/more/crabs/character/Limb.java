package msifeed.mc.more.crabs.character;

import msifeed.mc.sys.utils.L10n;

public enum Limb {
    HANDS(Ability.STR),
    LEGS(Ability.REF),
    FEELINGS(Ability.PER),
    ORGANS(Ability.END);

    public final Ability affectedAbility;

    Limb(Ability affectedAbility) {
        this.affectedAbility = affectedAbility;
    }

    public String tr() {
        return L10n.fmt("more.limb." + name().toLowerCase());
    }
    public String debuffTr(boolean both) {
        return L10n.fmt("more.limb.debuff." + name().toLowerCase() + (both ? ".both" : ""));
    }
}
