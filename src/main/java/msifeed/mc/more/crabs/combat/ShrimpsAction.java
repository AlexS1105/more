package msifeed.mc.more.crabs.combat;

public class ShrimpsAction {
    public Type type;
    public String mainAction = "";
    public String secondaryAction = "";
    public String swiftAction = "";
    public String freeAction = "";
    public String reaction = "";
    public String fullAction = "";
    public String initiative = "";

    public enum Type {
        SKIP,
        TURN,
        REACTION,
        FULL,
        NARRATIVE,
        INITIATIVE;
    }
}
