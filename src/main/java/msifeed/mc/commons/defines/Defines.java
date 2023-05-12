package msifeed.mc.commons.defines;

import msifeed.mc.extensions.chat.SpeechatDefines;
import msifeed.mc.extensions.locks.LocksDefines;
import msifeed.mc.more.content.ItemDefines;
import msifeed.mc.more.crabs.character.StatDefines;
import msifeed.mc.more.crabs.combat.CombatDefines;
import msifeed.mc.sys.config.ConfigBuilder;
import msifeed.mc.sys.config.JsonConfig;

public class Defines {
    private JsonConfig<DefinesContent> config = ConfigBuilder.of(DefinesContent.class, "defines.json").sync().create();
    private JsonConfig<CombatDefines> combat = ConfigBuilder.of(CombatDefines.class, "combat.json").sync().create();
    private JsonConfig<StatDefines> stats = ConfigBuilder.of(StatDefines.class, "stats.json").sync().create();

    public Defines() {
    }

    public DefinesContent get() {
        return config.get();
    }

    public CombatDefines combat() {
        return combat.get();
    }

    public StatDefines stats() {
        return stats.get();
    }

    public static final class DefinesContent {
        public SpeechatDefines chat = new SpeechatDefines();
        public LocksDefines locks = new LocksDefines();
        public ItemDefines items = new ItemDefines();
        public String characterPageUrl = "https://crust.ariadna.su/characters/";
    }
}
