package msifeed.mc.extensions.chat.formatter;

import msifeed.mc.extensions.chat.GmSpeech;
import msifeed.mc.more.crabs.character.Ability;
import msifeed.mc.more.crabs.character.Skill;
import msifeed.mc.more.crabs.rolls.Modifiers;
import msifeed.mc.more.crabs.rolls.Rolls;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public final class MiscFormatter {
    public static IChatComponent formatGmSay(GmSpeech.Preferences prefs, IChatComponent input) {
        final String prefix = prefs.prefix + (prefs.prefix.isEmpty() ? "" : " ");
        final IChatComponent cc = new ChatComponentText(prefix);
        cc.getChatStyle().setColor(prefs.color);
        cc.appendSibling(input);
        return cc;
    }

    public static IChatComponent formatOfftop(String name, String text) {
        return formatType("OFF", EnumChatFormatting.GRAY, name + ": " + text);
    }

    public static IChatComponent formatGlobal(String name, String text) {
        return formatType("GLOBAL", EnumChatFormatting.GREEN, name + ": " + text);
    }

    public static IChatComponent formatGmGlobal(String name, String text) {
        return formatType("GM", EnumChatFormatting.DARK_GREEN, name + ": " + text);
    }

    public static IChatComponent formatRoll(String name, int sides, int roll) {
        final String text = String.format("%s: d%d = %d", name, sides, roll);
        return formatType("ROLL", EnumChatFormatting.GOLD, text);
    }

    public static IChatComponent formatAbilityRoll(String name, Ability a, Modifiers m, Rolls.Result roll) {
        final String fmtRoll = roll.format(m.roll, m.toAbility(a), a);
        final String text = String.format("%s: %s %s", name, a.trShort(), fmtRoll);
        return formatType("ROLL", EnumChatFormatting.GOLD, text);
    }

    public static IChatComponent formatSkillRoll(String name, Skill s, Modifiers m, Rolls.Result roll) {
        final String fmtRoll = roll.format(m.roll, m.toAbility(s.stat), s.stat);
        final String text = String.format("%s: %s %s", name, s.name, fmtRoll);
        return formatType("ROLL", EnumChatFormatting.GOLD, text);
    }

    public static IChatComponent formatCombat(String text) {
        return formatType("COMBAT", EnumChatFormatting.GOLD, text);
    }

    public static IChatComponent formatLog(String name, String text) {
        return formatType("LOG", EnumChatFormatting.GRAY, name + ": " + text);
    }

    public static IChatComponent formatEnv(String text) {
        return formatType("ENV", EnumChatFormatting.WHITE, text);
    }

    private static IChatComponent formatType(String type, EnumChatFormatting color, String text) {
        final ChatComponentText cc = new ChatComponentText("[" + type + "] ");
        cc.getChatStyle().setColor(color);
        cc.appendSibling(new ChatComponentText(text));
        return cc;
    }
}
