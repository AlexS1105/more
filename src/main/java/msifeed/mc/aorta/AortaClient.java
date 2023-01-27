package msifeed.mc.aorta;

import com.google.common.io.CharStreams;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import msifeed.mc.Bootstrap;
import msifeed.mc.aorta.client.Keybinds;
import msifeed.mc.aorta.client.ResponsiveEntityStatus;
import msifeed.mc.extensions.chat.Speechat;
import msifeed.mc.extensions.itemmeta.ItemMetaClient;
import msifeed.mc.extensions.tweaks.GameWindowOptions;
import msifeed.mc.genesis.GenesisCreativeTab;
import msifeed.mc.mellow.Mellow;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class AortaClient extends Aorta {
    private GameWindowOptions gameWindowOptions = new GameWindowOptions();
    private ItemMetaClient itemMeta = new ItemMetaClient();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        gameWindowOptions.preInit();
        super.preInit(event);
        initMellow();
    }

    @Override
    public void init() {
        super.init();
        GenesisCreativeTab.init();
        GUI_HANDLER.init();
        Keybinds.INSTANCE.init();
        ResponsiveEntityStatus.init();
        Speechat.initClient();
        itemMeta.init();
    }

    private void initMellow() {
        final IResourcePack resourcePack = FMLClientHandler.instance().getResourcePackFor(Bootstrap.MODID);
        final ResourceLocation themeSprite = new ResourceLocation(Bootstrap.MODID + ":theme/theme.png");
        final ResourceLocation themeMeta = new ResourceLocation(Bootstrap.MODID + ":theme/theme.json");

        try {
            final InputStream metaInput = resourcePack.getInputStream(themeMeta);
            final String json = CharStreams.toString(new InputStreamReader(metaInput, StandardCharsets.UTF_8));
            Mellow.loadTheme(themeSprite, json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
