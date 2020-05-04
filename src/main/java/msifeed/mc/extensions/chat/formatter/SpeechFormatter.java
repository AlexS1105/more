package msifeed.mc.extensions.chat.formatter;

import msifeed.mc.extensions.chat.SpeechatDefines;
import msifeed.mc.more.More;
import msifeed.mc.sys.utils.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class SpeechFormatter {
    public static IChatComponent format(EntityPlayer sender, IChatComponent cc, int range) {
        final EntityPlayer self = Minecraft.getMinecraft().thePlayer;
        final boolean isMyMessage = self.getEntityId() == sender.getEntityId();

        final IChatComponent textCC;
        if (isMyMessage) {
            textCC = cc;
        } else {
            final double distance = self.getDistanceToEntity(sender);
            textCC = makeTextCC(cc, distance, range);
        }

        return new ChatComponentTranslation(
                "more.speechat.msg",
                makeNamePrefix(sender, isMyMessage), textCC
        );
    }

    private static IChatComponent makeNamePrefix(EntityPlayer player, boolean isMyName) {
        final IChatComponent cc = new ChatComponentText(ChatUtils.getPrettyName(player));
        cc.getChatStyle().setColor(isMyName ? EnumChatFormatting.YELLOW : EnumChatFormatting.GREEN);
        return cc;
    }

    private static IChatComponent makeTextCC(IChatComponent cc, double distance, int range) {
        final int thresholdDistance = More.DEFINES.get().chat.garble.thresholdDistance;

        if (distance > thresholdDistance) {
            final double garblness = (distance - thresholdDistance) / (double) range;
            if (cc instanceof ChatComponentText)
                return garbleficateText((ChatComponentText) cc, garblness);
            else if (cc instanceof ChatComponentTranslation)
                return garbleficateTranslation((ChatComponentTranslation) cc, garblness);
            else
                return cc;
        } else {
            return cc;
        }
    }

    private static IChatComponent garbleficateText(ChatComponentText input, double garblness) {
        final IChatComponent cc = garbleficateString(input.getChatComponentText_TextValue(), garblness);
        cc.setChatStyle(input.getChatStyle());
        for (IChatComponent c : garbleficateSiblings(input, garblness))
            cc.appendSibling(c);
        return cc;
    }

    private static IChatComponent garbleficateTranslation(ChatComponentTranslation cc, double garblness) {
        final Object[] args = cc.getFormatArgs();
        for (int i = 0; i < args.length; i++) {
            final Object arg = args[i];
            if (arg instanceof ChatComponentText)
                args[i] = garbleficateText((ChatComponentText) arg, garblness);
            else if (arg instanceof ChatComponentTranslation)
                args[i] = garbleficateTranslation((ChatComponentTranslation) arg, garblness);
//            else if (arg instanceof String)
//                args[i] = garbleficateString((String) arg, garblness);
        }
        final List<IChatComponent> newSiblings = garbleficateSiblings(cc, garblness);
        cc.getSiblings().clear();
        for (IChatComponent c : newSiblings)
            cc.appendSibling(c);
        return cc;
    }

    private static List<IChatComponent> garbleficateSiblings(IChatComponent input, double garblness) {
        final List<IChatComponent> newSiblings = new ArrayList<>();

        for (IChatComponent cc : (List<IChatComponent>) input.getSiblings()) {
            if (cc instanceof ChatComponentText)
                newSiblings.add(garbleficateText((ChatComponentText) cc, garblness));
            else if (cc instanceof ChatComponentTranslation)
                newSiblings.add(garbleficateTranslation((ChatComponentTranslation) cc, garblness));
            else
                newSiblings.add(cc);
        }

        return newSiblings;
    }

    private static ChatComponentText garbleficateString(String input, double garblness) {
        if (input.isEmpty())
            return new ChatComponentText("");

        final SpeechatDefines.GarbleSettings settings = More.DEFINES.get().chat.garble;

        final Random rand = new Random();

        final ChatComponentText root = new ChatComponentText("");
        final EnumChatFormatting[] prevColor = {null};
        final StringBuilder sb = new StringBuilder();

        input.codePoints().forEach(cp -> {
            final double r = garblness + rand.nextFloat() / 2;

            final EnumChatFormatting color;
            if (r > settings.missThreshold) {
                color = prevColor[0];
            } else if (r > settings.darkGrayThreshold) {
                color = EnumChatFormatting.DARK_GRAY;
            } else if (r > settings.grayThreshold) {
                color = EnumChatFormatting.GRAY;
            } else {
                color = null;
            }

            if (color != prevColor[0] && sb.length() > 0) {
                final ChatComponentText cc = new ChatComponentText(sb.toString());
                cc.getChatStyle().setColor(prevColor[0]);
                root.appendSibling(cc);

                sb.setLength(0);
            }

            if (Character.isLetterOrDigit(cp) && r > settings.missThreshold) {
                sb.append(' ');
            } else {
                sb.appendCodePoint(cp);
            }

            prevColor[0] = color;
        });

        if (sb.length() > 0) {
            final ChatComponentText cc = new ChatComponentText(sb.toString());
            cc.getChatStyle().setColor(prevColor[0]);
            root.appendSibling(cc);
        }

        return root;
    }

    public static int getSpeechRange(String text) {
        final int[] ranges = More.DEFINES.get().chat.speechRadius;
        int loudness = (ranges.length - 1) / 2;

        int exclamations = 0;
        for (int i = text.length() - 1; i >= 0; --i) {
            switch (text.charAt(i)) {
                case '!':
                    exclamations++;
                case '?':
                    continue;
            }
            break;
        }
        loudness += exclamations;

        if (exclamations == 0) {
            int silencers = 0;
            for (int i = 0; i < text.length(); ++i) {
                if (text.charAt(i) == '(') silencers++;
                else break;
            }
            loudness -= silencers;
        }

        loudness = MathHelper.clamp_int(loudness, 0, ranges.length - 1);

        return ranges[loudness];
    }
}
