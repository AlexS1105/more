package msifeed.mc.core;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.DependsOn("forge")
@IFMLLoadingPlugin.TransformerExclusions({"msifeed.mc.core"})
public class MoreCorePlugin implements IFMLLoadingPlugin {
    static boolean isDevEnv = false;

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                JourneymapTransformer.class.getName(),
                GlobalRenderTransformer.class.getName()
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        isDevEnv = !(boolean) data.get("runtimeDeobfuscationEnabled");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
