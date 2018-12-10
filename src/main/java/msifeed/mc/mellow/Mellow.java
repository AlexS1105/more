package msifeed.mc.mellow;

import msifeed.mc.mellow.theme.Part;
import msifeed.mc.mellow.theme.Theme;
import net.minecraft.util.ResourceLocation;

public class Mellow {
    public static Theme THEME = null;

    public static Part getPart(String name) {
        return THEME.parts.get(name);
    }

    public static void loadTheme(ResourceLocation sprite, String metaJson) {
        THEME = Theme.load(sprite, metaJson);
    }
}
