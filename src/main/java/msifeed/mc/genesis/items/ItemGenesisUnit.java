package msifeed.mc.genesis.items;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import msifeed.mc.genesis.GenesisTrait;
import msifeed.mc.genesis.GenesisUnit;
import msifeed.mc.genesis.items.data.CrabsData;
import msifeed.mc.genesis.items.data.ItemDurabilityData;
import msifeed.mc.genesis.items.data.ItemRenderData;
import msifeed.mc.sys.utils.JsonUtils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static msifeed.mc.genesis.GenesisTrait.*;

public class ItemGenesisUnit extends GenesisUnit {
    public String title;
    public String[] desc;
    public LinkedHashMap<String, String> values = new LinkedHashMap<>();
    public String texture;
    public ItemRarity rarity;
    public ItemRenderData renderData = null;
    public ItemDurabilityData durData = new ItemDurabilityData();
    public CrabsData crabsData = new CrabsData();
    public int maxUsages = 0;
    public int specialAttackCost = 0;
    public float attackDamage = 0;
    public String repairItem;

    public ItemGenesisUnit(JsonObject json, HashSet<GenesisTrait> traits) {
        super(json, traits);
        JsonUtils.consumeString(json, Props.title, s -> title = s);
        JsonUtils.consumeString(json, Props.desc, s -> desc = parseDescription(s));
        loadDescriptionValues(json);
        JsonUtils.consumeString(json, Props.texture, s -> texture = s);

        rarity = getRarity();
        if (rarity == ItemRarity.COMMON)
            traits.add(common);

        if (json.has(Props.render))
            renderData = new ItemRenderData(json);
        if (json.has(Props.durability))
            durData = new ItemDurabilityData(json);
        if (json.has(Props.crabs))
            crabsData = new CrabsData(json.getAsJsonObject(Props.crabs));

        if (json.has(Props.usages))
            JsonUtils.consumeInt(json, Props.usages, i -> maxUsages = i);

        JsonUtils.consumeFloat(json, Props.attackDamage, s -> attackDamage = s);
        JsonUtils.consumeString(json, Props.repairItem, s -> repairItem = s);
    }

    private void loadDescriptionValues(JsonObject json) {
        if (!json.has(Props.values) || !json.get(Props.values).isJsonObject())
            return;

        final JsonObject values = json.get(Props.values).getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            if (!entry.getValue().isJsonPrimitive() || !entry.getValue().getAsJsonPrimitive().isString())
                continue;
            this.values.put(entry.getKey(), entry.getValue().getAsString());
        }
    }

    private static String[] parseDescription(String raw) {
        return raw.split("\n");
    }

    private ItemRarity getRarity() {
        if (hasTrait(legendary))
            return ItemRarity.LEGENDARY;
        else if (hasTrait(epic))
            return ItemRarity.EPIC;
        else if (hasTrait(rare))
            return ItemRarity.RARE;
        else if (hasTrait(uncommon))
            return ItemRarity.UNCOMMON;
        else if (hasTrait(poor))
            return ItemRarity.POOR;
        else
            return ItemRarity.COMMON;
    }

    private static class Props {
        static final String title = "title";
        static final String desc = "description";
        static final String values = "values";
        static final String texture = "texture";
        static final String render = "render";
        static final String durability = "durability";
        static final String usages = "usages";
        static final String crabs = "crabs";
        static final String specialAttackCost = "specialAttackCost";
        static final String sanity = "sanity";
        static final String attackDamage = "attackDamage";
        static final String repairItem = "repairItem";
    }
}
