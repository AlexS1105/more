package msifeed.mc.extensions.chat.commands;

import msifeed.mc.commons.logs.ExternalLogs;
import msifeed.mc.extensions.chat.SpeechatRpc;
import msifeed.mc.more.More;
import msifeed.mc.sys.cmd.PlayerExtCommand;
import msifeed.mc.sys.utils.ChatUtils;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentText;

import java.util.Collections;
import java.util.List;

public class ShoutCommand extends PlayerExtCommand {
    @Override
    public String getCommandName() {
        return "shout";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("sh");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/shout <text>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0 || !(sender instanceof EntityPlayerMP))
            return;

        final int[] ranges = More.DEFINES.get().chat.speechRadius;
        final int rangeLevel = +2;
        final int range = ranges[(ranges.length - 1) / 2 + rangeLevel];

        final EntityPlayerMP player = (EntityPlayerMP) sender;
        final String text = String.join(" ", args);
        SpeechatRpc.sendSpeech(player, range, new ChatComponentText(ChatUtils.fromAmpersandFormatting(text)));
        ExternalLogs.log(sender, "speech",  text);
    }
}
