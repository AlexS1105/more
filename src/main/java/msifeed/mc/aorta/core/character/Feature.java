package msifeed.mc.aorta.core.character;

public enum Feature {
    STRENGTH, DEXTERITY, TENACITY, INTELLIGENCE;

    @Override
    public String toString() {
        switch (this) {
            case STRENGTH:
                return "Strength";
            case DEXTERITY:
                return "Dexterity";
            case TENACITY:
                return "Tenacity";
            case INTELLIGENCE:
                return "Intelligence";
            default:
                return super.toString();
        }
    }
}
