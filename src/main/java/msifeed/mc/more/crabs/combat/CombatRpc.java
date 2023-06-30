package msifeed.mc.more.crabs.combat;

import msifeed.mc.Bootstrap;
import msifeed.mc.commons.traits.Trait;
import msifeed.mc.more.More;
import msifeed.mc.more.crabs.action.Action;
import msifeed.mc.more.crabs.action.ActionRegistry;
import msifeed.mc.more.crabs.action.ActionTag;
import msifeed.mc.more.crabs.action.effects.Buff;
import msifeed.mc.more.crabs.action.effects.Effect;
import msifeed.mc.more.crabs.action.parser.EffectStringParser;
import msifeed.mc.more.crabs.character.Character;
import msifeed.mc.more.crabs.meta.MetaInfo;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.more.crabs.utils.CombatAttribute;
import msifeed.mc.more.crabs.utils.GetUtils;
import msifeed.mc.more.crabs.utils.MetaAttribute;
import msifeed.mc.sys.rpc.RpcContext;
import msifeed.mc.sys.rpc.RpcException;
import msifeed.mc.sys.rpc.RpcMethodHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public enum CombatRpc {
    INSTANCE;

    private final static String doAction = Bootstrap.MODID + ":combat.action.do";
    private final static String endAttack = Bootstrap.MODID + ":combat.endAttack";

    private final static String join = Bootstrap.MODID + ":combat.join";
    private final static String leave = Bootstrap.MODID + ":combat.leave";
    private final static String reset = Bootstrap.MODID + ":combat.reset";

    private final static String addCombat = Bootstrap.MODID + ":combat.add";
    private final static String removeCombat = Bootstrap.MODID + ":combat.remove";

    private final static String setPuppet = Bootstrap.MODID + ":combat.set.puppet";

    private final static String addBuff = Bootstrap.MODID + ":combat.buff.add";
    private final static String removeBuff = Bootstrap.MODID + ":combat.buff.remove";

    private final static String doShrimpsAction = Bootstrap.MODID + ":combat.shrimps.action";

    // // // //
    public static void doShrimpsAction(ShrimpsAction action) { More.RPC.sendToServer(doShrimpsAction, action); }

    @RpcMethodHandler(doShrimpsAction)
    public void onDoShrimpsAction(RpcContext ctx, ShrimpsAction action) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;

        CombatNotifications.shrimpsAction(sender, action);
    }

    // // // //

    public static void doAction(int entityId, String actionId) {
        More.RPC.sendToServer(doAction, entityId, actionId);
    }

    @RpcMethodHandler(doAction)
    public void onDoAction(RpcContext ctx, int entityId, String actionId) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        final Action action = ActionRegistry.getFullAction(actionId);
        if (action == null || action.hasAnyTag(ActionTag.hidden))
            throw new RpcException(sender, "unknown action: " + actionId);

        final CombatContext com = CombatAttribute.get(target)
                .orElseThrow(() -> new RpcException(sender, "target is not a combatant"));

        if (!com.phase.isInCombat())
            throw new RpcException(sender, "target is not in combat");

        CombatManager.INSTANCE.doAction(target, com, action);
    }

    public static void endAttack(int entityId) {
        More.RPC.sendToServer(endAttack, entityId);
    }

    @RpcMethodHandler(endAttack)
    public void onEndAttack(RpcContext ctx, int entityId) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        final CombatContext com = CombatAttribute.get(target)
                .orElseThrow(() -> new RpcException(sender, "target is not a combatant"));

        if (com.phase != CombatContext.Phase.ATTACK)
            throw new RpcException(sender, "target is not attacking!");

        CombatManager.INSTANCE.endAction(target, com);
    }

    // // // //

    public static void join(int entityId) {
        More.RPC.sendToServer(join, entityId, false);
    }
    public static void training(int entityId) {
        More.RPC.sendToServer(join, entityId, true);
    }

    @RpcMethodHandler(join)
    public void onJoin(RpcContext ctx, int entityId, boolean training) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        final CombatContext com = CombatAttribute.get(target).orElse(null);
        if (com == null)
            throw new RpcException(sender, "target is not a combatant");
        if (com.phase.isInCombat())
            throw new RpcException(sender, "target is already in combat");

        com.hardReset();
        com.healthBeforeJoin = training ? target.getHealth() : 0;
        CombatManager.INSTANCE.joinCombat(target, com);
    }

    public static void leave(int entityId) {
        More.RPC.sendToServer(leave, entityId);
    }

    @RpcMethodHandler(leave)
    public void onLeave(RpcContext ctx, int entityId) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        final CombatContext com = CombatAttribute.get(target).orElse(null);
        if (com == null)
            throw new RpcException(sender, "target is not a combatant");
        if (!com.phase.isInCombat())
            throw new RpcException(sender, "target is not in combat");
        if (com.phase != CombatContext.Phase.IDLE)
            throw new RpcException(sender, "target is not in " + CombatContext.Phase.IDLE.toString() + " stage");

        CombatManager.INSTANCE.removeFromCombat(target, com);
    }

    public static void reset(int entityId) {
        More.RPC.sendToServer(reset, entityId);
    }

    @RpcMethodHandler(reset)
    public void onReset(RpcContext ctx, int entityId) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        final CombatContext com = CombatAttribute.get(target).orElse(null);
        if (com == null)
            throw new RpcException(sender, "target is not a combatant");
        CombatManager.resetCombatantWithRelatives(target);
    }

    // // // //

    public static void addCombatToEntity(int entityId) {
        More.RPC.sendToServer(addCombat, entityId);
    }

    public static void removeCombatFromEntity(int entityId) {
        More.RPC.sendToServer(removeCombat, entityId);
    }

    @RpcMethodHandler(addCombat)
    public void onAddCombatToEntity(RpcContext ctx, int entityId) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .filter(e -> !(e instanceof EntityPlayer))
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        CharacterAttribute.INSTANCE.set(target, new Character());
        MetaAttribute.INSTANCE.set(target, new MetaInfo());
        CombatAttribute.INSTANCE.set(target, new CombatContext());
    }

    @RpcMethodHandler(removeCombat)
    public void onRemoveCombatFromEntity(RpcContext ctx, int entityId) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .filter(e -> !(e instanceof EntityPlayer))
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        CharacterAttribute.INSTANCE.set(target, null);
        MetaAttribute.INSTANCE.set(target, null);
        CombatAttribute.INSTANCE.set(target, null);
    }

    // // // //

    public static void setPuppet(int entityId) {
        More.RPC.sendToServer(setPuppet, entityId);
    }

    @RpcMethodHandler(setPuppet)
    public void onSetPuppet(RpcContext ctx, int entityId) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;

        if (sender.getEntityId() == entityId)
            throw new RpcException(sender, "you can't puppet yourself, silly");

        if (entityId != 0) {
            GetUtils.entityLiving(sender, entityId)
                    .orElseThrow(() -> new RpcException(sender, "puppet target is not found"));
        }

        CombatAttribute.INSTANCE.update(sender, context -> context.puppet = entityId);
    }

    // // // //

    public static void addBuff(int entityId, Buff buff) {
        More.RPC.sendToServer(addBuff, entityId, buff.encode());
    }

    public static void removeBuff(int entityId, int index) {
        More.RPC.sendToServer(removeBuff, entityId, index);
    }

    @RpcMethodHandler(addBuff)
    public void onAddBuff(RpcContext ctx, int entityId, String buffLine) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        if (!CharacterAttribute.has(sender, Trait.gm))
            throw new RpcException(sender, "you are not GM");

        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        final CombatContext com = CombatAttribute.get(target).orElse(null);
        if (com == null)
            throw new RpcException(sender, "target is not a combatant");

        final Buff buff;
        try {
            final Effect effect = EffectStringParser.parseEffect(buffLine);
            if (effect instanceof Buff)
                buff = (Buff) effect;
            else
                throw new RpcException(sender, "provided effect is not a buff");
        } catch (RpcException e) {
            throw e;
        } catch (Exception e) {
            throw new RpcException(sender, "can't decode buff");
        }

        com.buffs.add(buff);
        CombatAttribute.INSTANCE.set(target, com);
    }

    @RpcMethodHandler(removeBuff)
    public void onRemoveBuff(RpcContext ctx, int entityId, int index) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        if (!CharacterAttribute.has(sender, Trait.gm))
            throw new RpcException(sender, "you are not GM");

        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        final CombatContext com = CombatAttribute.get(target).orElse(null);
        if (com == null)
            throw new RpcException(sender, "target is not a combatant");

        if (index < 0 || index >= com.buffs.size())
            throw new RpcException(sender, "invalid index");

        com.buffs.remove(index);
        CombatAttribute.INSTANCE.set(target, com);
    }
}
