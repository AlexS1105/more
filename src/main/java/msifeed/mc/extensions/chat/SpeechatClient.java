package msifeed.mc.extensions.chat;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import msifeed.mc.Bootstrap;
import msifeed.mc.commons.traits.Trait;
import msifeed.mc.extensions.chat.formatter.SpeechFormatter;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.more.crabs.utils.GetUtils;
import msifeed.mc.more.crabs.utils.MetaAttribute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;

@SideOnly(Side.CLIENT)
final class SpeechatClient {
    static void receiveSpeech(int senderId, IChatComponent cc, int range) {
        final EntityPlayer self = Minecraft.getMinecraft().thePlayer;
        final EntityPlayer sender = GetUtils.entityPlayer(self, senderId).orElse(null);
        if (sender == null)
            return;

        IChatComponent component = SpeechFormatter.format(self, sender, cc, range);
        if (component.getUnformattedText().trim().isEmpty()) {
            return;
        }

        self.addChatMessage(component);
        playNotificationSound(sender);
    }

    static void receiveGlobal(IChatComponent cc) {
        final EntityPlayer self = Minecraft.getMinecraft().thePlayer;
        if (MetaAttribute.require(self).receiveGlobal)
            displayMessage(self, cc);
    }

    static void receiveGmGlobal(IChatComponent cc) {
        final EntityPlayer self = Minecraft.getMinecraft().thePlayer;
        if (CharacterAttribute.hasAny(self, Trait.__admin, Trait.gm))
            displayMessage(self, cc);
    }

    static void receiveRaw(IChatComponent cc) {
        final EntityPlayer self = Minecraft.getMinecraft().thePlayer;
        displayMessage(self, cc);
    }

    private static void displayMessage(EntityPlayer player, IChatComponent cc) {
        player.addChatMessage(cc);
        playNotificationSound(player);
    }

    private static void playNotificationSound(EntityPlayer p) {
        final WorldClient w = FMLClientHandler.instance().getWorldClient();
        w.playSound(p.posX, p.posY, p.posZ, Bootstrap.MODID + ":speechat.message", 1.0F, 0.7F, true);
    }
}
