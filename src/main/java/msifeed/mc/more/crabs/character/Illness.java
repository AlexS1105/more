package msifeed.mc.more.crabs.character;

public class Illness {
    private static final int MASK = 0x3ff; // 10 bits per value

    public short limit = 0;
    public short illness = 0;
    public short treatment = 0;
    public boolean limitVisible = false;

    public int pack() {
        return ((limitVisible ? 1 : 0) << 30)
                | (limit & MASK) << 20
                | (illness & MASK) << 10
                | treatment & MASK;
    }

    public void unpack(int i) {
        limitVisible = (i >> 30 & 1) == 1;
        limit = (short) ((i >> 20) & MASK);
        illness = (short) ((i >> 10) & MASK);
        treatment = (short) (i & MASK);
    }

    public boolean cured() {
        return limit > 0 && treatment >= limit;
    }

    public boolean lost() {
        return limit > 0 && illness >= limit;
    }

    /// На отрезке [0, 7] от плохого самочувствия к хорошему
    public int level() {
        final int diff = treatment - illness;
        if (diff >= 50)
            return 7;
        else if (diff >= 30)
            return 6;
        else if (diff >= 10)
            return 5;
        else if (diff >= 0)
            return 4;
        else if (diff >= -10)
            return 3;
        else if (diff >= -30)
            return 2;
        else if (diff >= -50)
            return 1;
        else
            return 0;
    }

    public int debuff() {
        return 0;
    }
}
