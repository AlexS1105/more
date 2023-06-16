package msifeed.mc.more.crabs.character;

import msifeed.mc.sys.utils.L10n;

import java.util.List;

public enum Trauma {
    DEBUFF_STR, DEBUFF_END, DEBUFF_PER, DEBUFF_REF,
    DEBUFF_DET, DEBUFF_INT, DEBUFF_WIL, DEBUFF_SPR,
    DEBUFF_BURN, DEBUFF_AMNESIA;

    public String trShort() {
        return L10n.fmt("more.trauma.short." + name().toLowerCase());
    }
}
