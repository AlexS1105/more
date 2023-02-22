package msifeed.mc.more.crabs.crust;

import msifeed.mc.more.crabs.character.Ability;

import java.util.HashMap;
import java.util.Map;

public class CrustCharsheet {
    public Map<String, Integer> stats = new HashMap<>();

    public static String ability2crust(Ability a) {
        switch (a) {
            case STR:
                return "strength";
            case END:
                return "endurance";
            case PER:
                return "perception";
            case REF:
                return "agility"; // fix this?
            case DET:
                return "determination";
            case INT:
                return "erudition"; // fix this?
            case WIL:
                return "will";
            case SPR:
                return "potential"; // fix this
            default:
                return "";
        }
    }
}
