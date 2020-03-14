package msifeed.mc.more.crabs.action;

public enum ActionTag {
    melee, ranged, magical, intellectual,
    defencive,
    none, equip, reload,
    ;

    public boolean isType() {
        return ordinal() <= intellectual.ordinal();
    }
}
