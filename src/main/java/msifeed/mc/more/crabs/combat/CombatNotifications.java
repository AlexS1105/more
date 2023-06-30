package msifeed.mc.more.crabs.combat;

import cpw.mods.fml.common.network.NetworkRegistry;
import msifeed.mc.commons.logs.ExternalLogs;
import msifeed.mc.extensions.chat.SpeechatRpc;
import msifeed.mc.extensions.chat.formatter.MiscFormatter;
import msifeed.mc.more.More;
import msifeed.mc.more.crabs.action.ActionHeader;
import msifeed.mc.more.crabs.rolls.Criticalness;
import msifeed.mc.sys.utils.ChatUtils;
import msifeed.mc.sys.utils.L10n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

import java.util.Set;
import java.util.stream.Collectors;

public final class CombatNotifications {
    public static void shrimpsAction(EntityPlayer sender, ShrimpsAction action) {
        switch (action.type) {
            case SKIP:
                notifyShrimps(sender, L10n.fmt("more.shrimps.skip_turn"));
                break;
            case NARRATIVE:
                notifyShrimps(sender, L10n.fmt("more.shrimps.narrative_action"));
                break;
            case REACTION:
                if (!action.reaction.trim().isEmpty()) {
                    notifyShrimps(sender, L10n.fmt("more.shrimps.reaction", action.reaction));
                }
                break;
            case FULL:
                if (!action.fullAction.trim().isEmpty()) {
                    notifyShrimps(sender, L10n.fmt("more.shrimps.full_action", action.fullAction));
                }
                break;
            case TURN:
                notifyTurnActions(sender, action);
                break;
        }
    }

    private static void notifyTurnActions(EntityPlayer sender, ShrimpsAction action) {
        boolean hasMainAction = !action.mainAction.trim().isEmpty();
        boolean hasSecondaryAction = !action.secondaryAction.trim().isEmpty();
        boolean hasSwiftAction = !action.swiftAction.trim().isEmpty();
        boolean hasFreeAction = !action.freeAction.trim().isEmpty();

        boolean hasFilledActions = hasMainAction || hasSecondaryAction || hasSwiftAction || hasFreeAction;
        if (hasFilledActions) {
            if (hasMainAction) {
                notifyShrimps(sender, L10n.fmt("more.shrimps.main_action", action.mainAction));
            }

            if (hasSecondaryAction) {
                notifyShrimps(sender, L10n.fmt("more.shrimps.secondary_action", action.secondaryAction));
            }

            if (hasSwiftAction) {
                notifyShrimps(sender, L10n.fmt("more.shrimps.swift_action", action.swiftAction));
            }

            if (hasFreeAction) {
                notifyShrimps(sender, L10n.fmt("more.shrimps.free_action", action.freeAction));
            }
        }
    }

    public static void actionChanged(EntityPlayer sender, ActionHeader action) {
        notify(sender, "§fВыбрано действие " + action.getTitle());
    }

    static void notifyKnockedOut(FighterInfo self) {
        notifyAroundRelatives(self, L10n.fmt("more.crabs.knocked_out", ChatUtils.getPrettyName(self.entity(), self.chr)));
    }

    static void notifyKilled(FighterInfo self) {
        notifyAroundRelatives(self, L10n.fmt("more.crabs.killed", ChatUtils.getPrettyName(self.entity(), self.chr)));
    }

    static void actionResult(FighterInfo winner, FighterInfo looser) {
        // Chad - LUCK Punch + Stun [50] (+5 STR+99) > Virgin - Roll [10]
        final String text = formatAction(winner) + " > " + formatAction(looser);
        notifyAroundRelatives(winner, text);
    }

    static void soloMoveResult(FighterInfo info) {
        // Virgin - FAIL Punch
        final String text = formatAction(info);

        notify(info.entity(), text);
    }

    private static String formatAction(FighterInfo info) {
        return ChatUtils.getPrettyName(info.entity()) + " - " + formatScores(info);
    }

    private static String formatScores(FighterInfo info) {
        final ActionContext act = info.act;
        final StringBuilder sb = new StringBuilder();

        if (!act.successful || act.critical == Criticalness.FAIL)
            sb.append("ПРОВАЛ ");
        else if (act.critical == Criticalness.LUCK)
            sb.append("УДАЧА ");

        sb.append(act.action.getTitle());
        if (info.comboAction != null) {
            sb.append(" + ");
            sb.append(info.comboAction.getTitle());
        }

        if (act.score() > 0) {
            if (act.scoreAction > 0) {
                sb.append(" [");
                sb.append(act.scoreAction);
                sb.append(']');
            }

            if (!info.mod.isZeroed()) {
                sb.append(" (");
                final int len = sb.length();
                if (info.mod.roll != 0)
                    sb.append(explicitSignInt(info.mod.roll + info.mod.metaRoll));
                if (info.mod.hasAbilityMods()) {
                    if (len != sb.length())
                        sb.append(' ');
                    sb.append(info.mod.abilities.entrySet().stream()
                            .filter(e -> e.getValue() != 0)
                            .map(e -> e.getKey().trShort() + explicitSignInt(e.getValue()))
                            .collect(Collectors.joining(" ")));
                }
                if (info.mod.damage != 0) {
                    if (len != sb.length())
                        sb.append(' ');
                    sb.append("DMG");
                    sb.append(explicitSignInt(info.mod.damage));
                }
                sb.append(')');
            }
        }

        return sb.toString();
    }

    public static String explicitSignInt(int i) {
        return String.format("%+d", i);
    }

    private static void notifyShrimps(EntityLivingBase entity, String text) {
        final int range = More.DEFINES.get().chat.combatRadius;
        SpeechatRpc.sendRaw(entity, range, MiscFormatter.formatShrimps(ChatUtils.getPrettyName(entity), text));
        ExternalLogs.logEntity(entity, "combat", text);
    }

    private static void notify(EntityLivingBase entity, String text) {
        final int range = More.DEFINES.get().chat.combatRadius;
        SpeechatRpc.sendRaw(entity, range, MiscFormatter.formatCombat(text));
        ExternalLogs.logEntity(entity, "combat", text);
    }

    public static void notifyAroundRelatives(FighterInfo cause, String text) {
        final EntityLivingBase causeEntity = cause.entity();
        final Set<EntityLivingBase> relatives = CombatUtils.relativeEntites(causeEntity, cause.com);

        long avgX = 0, avgY = 0, avgZ = 0;
        for (EntityLivingBase e : relatives) {
            avgX += e.posX;
            avgY += e.posY;
            avgZ += e.posZ;
        }
        avgX /= relatives.size();
        avgY /= relatives.size();
        avgZ /= relatives.size();

        int range = More.DEFINES.get().chat.combatRadius;
        for (EntityLivingBase e : relatives) {
            final int dist = MathHelper.ceiling_double_int(e.getDistance(avgX, avgY, avgZ)) + 5;
            if (dist > range)
                range = dist;
        }

        final NetworkRegistry.TargetPoint point = new NetworkRegistry.TargetPoint(causeEntity.dimension, avgX, avgY, avgZ, range);
        SpeechatRpc.sendRaw(point, MiscFormatter.formatCombat(text));
        ExternalLogs.logEntity(causeEntity, "combat", text);
    }
}
