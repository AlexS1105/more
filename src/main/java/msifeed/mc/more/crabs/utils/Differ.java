package msifeed.mc.more.crabs.utils;

import msifeed.mc.commons.logs.ExternalLogs;
import msifeed.mc.extensions.chat.SpeechatRpc;
import msifeed.mc.extensions.chat.formatter.MiscFormatter;
import msifeed.mc.more.More;
import msifeed.mc.more.crabs.character.Ability;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.character.Limb;
import msifeed.mc.more.crabs.character.Trauma;
import msifeed.mc.sys.utils.ChatUtils;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class Differ {
    public static String diff(Character before, Character after) {
        final ArrayList<String> diffs = new ArrayList<>();

        if (!before.name.equalsIgnoreCase(after.name))
            diffs.add(L10n.fmt("more.diff.status.name", after.name));

        for (Map.Entry<Ability, Integer> e : after.abilities.entrySet()) {
            final int b = before.abilities.get(e.getKey());
            final int a = e.getValue();
            if (b != a)
                diffs.add(L10n.fmt("more.diff.char.ability", e.getKey().toString(), b, a));
        }
        if (before.armor != after.armor) {
            diffs.add(L10n.fmt("more.diff.char.mod_armor", after.armor, trGenPoints(after.armor)));
        }
        if (before.damageThreshold != after.damageThreshold) {
            diffs.add(L10n.fmt("more.diff.char.mod_damage_threshold", after.damageThreshold, trGenPoints(after.damageThreshold)));
        }

        // Illness
        if (before.illness.illness < after.illness.illness)
            diffs.add(L10n.fmt("more.diff.status.illness.add_illness", after.illness.illness - before.illness.illness));
        if (before.illness.illness > after.illness.illness)
            diffs.add(L10n.fmt("more.diff.status.illness.rem_illness", before.illness.illness - after.illness.illness));
        if (before.illness.treatment < after.illness.treatment)
            diffs.add(L10n.fmt("more.diff.status.illness.add_treatment", after.illness.treatment - before.illness.treatment));
        if (before.illness.treatment > after.illness.treatment)
            diffs.add(L10n.fmt("more.diff.status.illness.rem_treatment", before.illness.treatment - after.illness.treatment));

        if (before.fistsDamage != after.fistsDamage) {
            diffs.add(L10n.fmt(
                    before.fistsDamage < after.fistsDamage ? "more.diff.status.add_fists_damage" : "more.diff.status.rem_fists_damage",
                    after.fistsDamage, trGenPoints(after.fistsDamage)
            ));
        }

        if (before.estitence != after.estitence) {
            final int n = Math.abs(before.estitence - after.estitence);
            diffs.add(L10n.fmt(
                    before.estitence < after.estitence ? "more.diff.status.add_estitence" : "more.diff.status.rem_estitence",
                    n, trNomPoints(n)
            ));
        }

        if (before.sin != after.sin) {
            final int n = Math.abs(before.sin - after.sin);
            diffs.add(L10n.fmt(
                    before.sin < after.sin ? "more.diff.status.add_sin" : "more.diff.status.rem_sin",
                    n, trNomPoints(n)
            ));
        }

        return String.join(", ", diffs);
    }

    private static String trNomPoints(int points) {
        switch (points) {
            case 11:
            case 12:
            case 13:
            case 14:
                return L10n.tr("more.diff.m");
            default:
                switch (points % 10) {
                    case 0:
                        return L10n.tr("more.diff.m");
                    case 1:
                        return L10n.tr("more.diff.s");
                    case 2:
                    case 3:
                    case 4:
                        return L10n.tr("more.diff.g");
                    default:
                        return L10n.tr("more.diff.m");
                }
        }
    }

    private static String trGenPoints(int points) {
        if (points == 1)
            return L10n.tr("more.diff.g");
        return L10n.tr("more.diff.m");
    }

    public static String diffResults(Character before, Character after) {
        final ArrayList<String> diffs = new ArrayList<>();

        // Illness
        if (before.illness.level() != after.illness.level())
            diffs.add(L10n.fmt("more.diff.status.illness." + after.illness.level()));
        if (!before.illness.cured() && after.illness.cured())
            diffs.add(L10n.tr("more.diff.status.illness.cured"));
        if (!before.illness.lost() && after.illness.lost())
            diffs.add(L10n.tr("more.diff.status.illness.lost"));
        if (before.illness.debuff() != after.illness.debuff())
            diffs.add(L10n.fmt("more.diff.status.illness.debuff", after.illness.debuff()));

        final float hpBefore = before.countMaxHealth();
        final float hpAfter = after.countMaxHealth();
        if (hpBefore != hpAfter) {
            final float n = Math.abs(hpBefore - hpAfter);
            diffs.add(L10n.fmt(
                    hpBefore < hpAfter ? "more.diff.status.add_max_health" : "more.diff.status.rem_max_health", n
            ));
        }

        final int sinAfter = after.sinLevel();
        if (before.sinLevel() != sinAfter) {
            diffs.add(L10n.fmt("more.diff.status.sin_level", L10n.tr("more.status.sin." + sinAfter)));
        }

        for (Map.Entry<Trauma, Integer> e : before.traumas.entrySet()) {
            Integer a = e.getValue();
            Integer b = after.traumas.get(e.getKey());

            if (!Objects.equals(a, b)) {
                if (b == 0) {
                    diffs.add(L10n.fmt("more.diff.status.traumas.zero", e.getKey().trShort()));
                    continue;
                }

                final int n = Math.abs(a - b);
                diffs.add(L10n.fmt(a < b
                        ? "more.diff.status.traumas.add_trauma"
                        : "more.diff.status.traumas.rem_trauma",
                        e.getKey().trShort(), n));
            }
        }

        int beforeIntox = 0;
        int afterIntox = 0;
        for (boolean b : before.intoxication) {
            beforeIntox += b ? 1 : 0;
        }
        for (boolean b : after.intoxication) {
            afterIntox += b ? 1 : 0;
        }

        if (beforeIntox != afterIntox) {
            final int n = Math.abs(beforeIntox - afterIntox);
            diffs.add(L10n.fmt(beforeIntox < afterIntox
                    ? "more.diff.status.intox.add_intox"
                    : "more.diff.status.intox.rem_intox",
                    n));
        }

        int beforeAttrib = 0;
        int afterAttrib = 0;
        for (boolean b : before.attribution) {
            beforeAttrib += b ? 1 : 0;
        }
        for (boolean b : after.attribution) {
            afterAttrib += b ? 1 : 0;
        }

        if (beforeAttrib != afterAttrib) {
            final int n = Math.abs(beforeAttrib - afterAttrib);
            diffs.add(L10n.fmt(beforeAttrib < afterAttrib
                    ? "more.diff.status.attrib.add_attrib"
                    : "more.diff.status.attrib.rem_attrib",
                    n));
        }

        for (Map.Entry<Limb, Integer> e : before.limbs.entrySet()) {
            int a = e.getValue();
            int b = after.limbs.get(e.getKey());

            if (!Objects.equals(a, b)) {
                String diff = L10n.fmt(a < b
                        ? "more.diff.status.limb.gained"
                        : "more.diff.status.limb.lost",
                        e.getKey().tr());

                if (Math.abs(a - b) == 2) {
                    diff += " (x2)";
                }

                diffs.add(diff);
            }
        }

        return String.join(", ", diffs);
    }

    public static void printDiffs(EntityPlayerMP sender, EntityLivingBase entity, Character before, Character after) {
        final String speaker = ChatUtils.getPrettyName(entity, before);
        final String logPrefix = sender == entity ? "" : "(" + sender.getDisplayName() + ") ";
        sendLogs(sender, speaker, logPrefix, diff(before, after));
        sendLogs(sender, speaker, logPrefix, diffResults(before, after));
    }

    private static void sendLogs(EntityPlayerMP sender, String speaker, String prefix, String message) {
        if (message.isEmpty())
            return;

        final int range = More.DEFINES.get().chat.logRadius;
        final String text = prefix + message;

        SpeechatRpc.sendRaw(sender, range, MiscFormatter.formatLog(speaker, text));
        ExternalLogs.log(sender, "log", text);
    }
}
