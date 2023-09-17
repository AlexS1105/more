package msifeed.mc.extensions.chat;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import msifeed.mc.commons.logs.ExternalLogs;
import msifeed.mc.extensions.chat.commands.*;
import msifeed.mc.extensions.chat.formatter.MiscFormatter;
import msifeed.mc.extensions.chat.formatter.SpeechFormatter;
import msifeed.mc.more.More;
import msifeed.mc.sys.utils.ChatUtils;
import net.minecraft.command.CommandHandler;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;

public class Speechat {
    private final SpeechatRpc speechatRpc = new SpeechatRpc();

    public void preInit() {
        if (FMLCommonHandler.instance().getSide().isClient())
            More.RPC.register(speechatRpc);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void registerCommands(CommandHandler handler) {
        handler.registerCommand(new WhisperCommand());
        handler.registerCommand(new YellCommand());
        handler.registerCommand(new ShoutCommand());
        handler.registerCommand(new HalfWhisperCommand());
        handler.registerCommand(new OfftopCommand());
        handler.registerCommand(new GlobalCommand());
        handler.registerCommand(new GmSayCommand());
        handler.registerCommand(new GmPmCommand());
        handler.registerCommand(new GmGlobalCommand());
        handler.registerCommand(new SuSayCommand());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onChatMessageSent(ServerChatEvent event) {
        final IChatComponent text = extractTextFromTranslation(event.component);
        if (text != null) {
            if (GmSpeech.shouldUseGmsay(event.player)) {
                final GmSpeech.Preferences prefs = GmSpeech.get(event.player.getCommandSenderName());
                final String formatted = ChatUtils.fromAmpersandFormatting(event.message);
                SpeechatRpc.sendRaw(event.player, prefs.range, MiscFormatter.formatGmSay(prefs, new ChatComponentText(formatted)));
                ExternalLogs.log(event.player, "gm", text.getUnformattedText());
            } else {
                final int range = SpeechFormatter.getSpeechRange(text.getUnformattedText());
                final String formatted = ChatUtils.fromAmpersandFormatting(event.message);
                SpeechatRpc.sendSpeech(event.player, range, new ChatComponentText(formatted));
                ExternalLogs.log(event.player, "speech", text.getUnformattedText());
            }
        }

        event.component = null;
        event.setCanceled(true);
    }

    private static IChatComponent extractTextFromTranslation(ChatComponentTranslation cc) {
        if (cc.getKey().equals("chat.type.text")) {
            final Object[] args = cc.getFormatArgs();
            if (args.length != 2)
                return null;
            final Object textArg = args[1]; // Pick "text" arg
            if (textArg instanceof IChatComponent)
                return (IChatComponent) textArg;
            else
                return new ChatComponentText(textArg.toString());
        } else {
            return cc;
        }
    }
}
