package msifeed.mc.more.crabs.combat;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import msifeed.mc.more.More;
import msifeed.mc.more.crabs.action.Action;
import msifeed.mc.more.crabs.action.ActionRegistry;
import msifeed.mc.more.crabs.action.ActionTag;
import msifeed.mc.more.crabs.action.Combo;
import msifeed.mc.more.crabs.action.effects.Buff;
import msifeed.mc.more.crabs.action.effects.Effect;
import msifeed.mc.more.crabs.rolls.Criticalness;
import msifeed.mc.more.crabs.rolls.Dices;
import msifeed.mc.more.crabs.utils.*;
import msifeed.mc.sys.attributes.MissingRequiredAttributeException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum CombatManager {
    INSTANCE;

    public void init() {
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);
    }

    public boolean doAction(EntityLivingBase self, CombatContext ctx, Action action) {
        final boolean actionChanged = ctx.action == null || !ctx.action.id.equals(action.id);

        if (ctx.phase == CombatContext.Phase.IDLE) {
            if (!action.isOffencive())
                return false;
            if (actionChanged)
                updateAction(self, action);
            else if (action.requiresNoRoll())
                endAction(self, ctx);
            return true;
        } else if (ctx.phase == CombatContext.Phase.DEFEND) {
            if (ctx.offender == 0)
                return false;
            final EntityLivingBase offender = GetUtils.entityLiving(self, ctx.offender).orElse(null);
            if (offender == null)
                return false;
            final CombatContext offenderCom = CombatAttribute.require(offender);
            if (offenderCom.phase != CombatContext.Phase.WAIT)
                return false;
            final ActionContext offenderAct = ActionAttribute.require(offender);
            if (!action.isValidDefencive(offenderAct.action.getType()))
                return false;

            if (actionChanged)
                updateAction(self, action);
            else {
                ctx.phase = CombatContext.Phase.WAIT;
                if (!tryFinishMove(offender, offenderCom))
                    CombatAttribute.INSTANCE.set(self, ctx);
            }
            return true;
        }
        return false;
    }

    private static void updateAction(EntityLivingBase self, Action action) {
        CombatAttribute.INSTANCE.update(self, com -> com.action = action);

        final ActionContext act = ActionAttribute.get(self).orElse(new ActionContext());
        act.action = action;
        ActionAttribute.INSTANCE.set(self, act);
    }

    public void endAction(EntityLivingBase self, CombatContext com) {
        if (com.action.requiresNoRoll()) {
            finishSoloMove(new FighterInfo(self));
        } else {
            com.phase = CombatContext.Phase.WAIT;
            CombatAttribute.INSTANCE.set(self, com);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onHurtDamage(LivingHurtEvent event) {
        if (event.entityLiving.worldObj.isRemote)
            return;
        try {
            final Entity srcEntity = event.source.getEntity();
            if (srcEntity instanceof EntityLivingBase)
                handleEntityDamage(event, (EntityLivingBase) srcEntity);
            else
                handleNeutralDamage(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleNeutralDamage(LivingHurtEvent event) {
        final EntityLivingBase vicEntity = event.entityLiving;

        final CombatContext vicCom = CombatAttribute.get(vicEntity).orElse(null);
        if (vicCom == null)
            return;
        if (vicCom.phase == CombatContext.Phase.END)
            return; // Pass full damage
        if (vicCom.offender != 0)
            return;

        final EntityLivingBase srcEntity;
        final CombatContext srcCom;
        try {
            srcEntity = GetUtils.entityLiving(vicEntity, vicCom.offender).orElse(null);
            if (srcEntity == null)
                return;
            srcCom = CombatAttribute.require(srcEntity);
        } catch (MissingRequiredAttributeException e) {
            return;
        }

        event.setCanceled(true);

        if (srcEntity == vicEntity) // Do not attack your puppet
            return;
        if (canNotDealDamage(srcCom, vicCom))
            return;

        vicCom.damageDealt.add(new DamageAmount(event.source, event.ammount));
        CombatAttribute.INSTANCE.set(vicEntity, vicCom);
    }

    private void handleEntityDamage(LivingHurtEvent event, EntityLivingBase damageSrcEntity) {
        final EntityLivingBase vicEntity = event.entityLiving;

        final CombatContext vicCom = CombatAttribute.get(vicEntity).orElse(null);
        if (vicCom == null)
            return;

        if (vicCom.phase == CombatContext.Phase.END)
            return; // Pass full damage

        final EntityLivingBase offEntity;
        final CombatContext offCom;
        try {
            final CombatContext directCom = CombatAttribute.require(damageSrcEntity);
            if (directCom.puppet != 0) {
                final EntityLivingBase puppet = GetUtils.entityLiving(damageSrcEntity, directCom.puppet).orElse(null);
                if (puppet == null)
                    return;
                offEntity = puppet;
                offCom = CombatAttribute.require(puppet);
            } else {
                offEntity = damageSrcEntity;
                offCom = directCom;
            }
        } catch (MissingRequiredAttributeException e) {
            return;
        }

        event.setCanceled(true);

        if (offEntity == vicEntity) // Do not attack your puppet
            return;
        if (canNotDealDamage(offCom, vicCom))
            return;

        boolean shouldUpdateOffender = false;
        if (offCom.phase != CombatContext.Phase.ATTACK) {
            offCom.role = CombatContext.Role.OFFENCE;
            offCom.phase = CombatContext.Phase.ATTACK;
            offCom.defenders = new ArrayList<>();
            shouldUpdateOffender = true;
        }
        if (!offCom.defenders.contains(vicEntity.getEntityId())) {
            offCom.defenders.add(vicEntity.getEntityId());
            shouldUpdateOffender = true;
        }
        if (shouldUpdateOffender)
            CombatAttribute.INSTANCE.set(offEntity, offCom);

        if (vicCom.phase != CombatContext.Phase.DEFEND) {
            CombatAttribute.INSTANCE.update(vicEntity, context -> {
                context.role = CombatContext.Role.DEFENCE;
                context.phase = CombatContext.Phase.DEFEND;
                context.offender = offEntity.getEntityId();
            });
        }

        final float fistsMod;
        if (offEntity.getHeldItem() == null)
            fistsMod = CharacterAttribute.get(offEntity).map(c -> c.fistsDamage).orElse(0);
        else
            fistsMod = 0;
        final float modMod = MetaAttribute.get(offEntity).map(m -> m.modifiers.damage).orElse(0);

        final float finalDamage = event.ammount + fistsMod + modMod;
        if (finalDamage <= 0)
            return;

        vicCom.damageDealt.add(new DamageAmount(event.source, finalDamage));
        CombatAttribute.INSTANCE.set(vicEntity, vicCom);
    }

    private boolean canNotDealDamage(CombatContext srcCom, CombatContext vicCom) {
        if (srcCom.isTraining() != vicCom.isTraining())
            return true; // Ignore if someone is not in training
        if (srcCom.phase != CombatContext.Phase.IDLE && srcCom.phase != CombatContext.Phase.ATTACK)
            return true;
        if (srcCom.action == null)
            return true;
        if (vicCom.phase != CombatContext.Phase.IDLE && vicCom.phase != CombatContext.Phase.DEFEND)
            return true;

        return false;
    }

    private boolean tryFinishMove(EntityLivingBase offender, CombatContext offenderCom) {
        final List<EntityLivingBase> defenderEntities =  offenderCom.defenders.stream()
                .map(id -> GetUtils.entityLiving(offender, id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        final List<CombatContext> defenderComs = defenderEntities.stream()
                .map(CombatAttribute::require)
                .collect(Collectors.toList());
        final boolean allDefendersAreWaiting = defenderComs.stream()
                .allMatch(ctx -> ctx.phase == CombatContext.Phase.WAIT);

        final boolean everyoneIsReady = defenderEntities.size() == defenderComs.size() && allDefendersAreWaiting;
        if (!everyoneIsReady)
            return false;

        finishMove(offender, defenderEntities);
        return true;
    }

    private void finishMove(EntityLivingBase offenderEntity, List<EntityLivingBase> defenderEntities) {
        final FighterInfo offender = new FighterInfo(offenderEntity);
        final List<FighterInfo> defenders = defenderEntities.stream().map(FighterInfo::new).collect(Collectors.toList());

        offender.com.phase = CombatContext.Phase.END;
        for (FighterInfo defender : defenders)
            defender.com.phase = CombatContext.Phase.END;

        applyScores(offender);
        for (FighterInfo defender : defenders) {
            do {
                applyScores(defender);
            } while (offender.act.compareTo(defender.act) == 0);
        }

        for (FighterInfo defender : defenders) {
            if (offender.act.compareTo(defender.act) > 0)
                applyEffectsAndResults(offender, defender);
            else
                applyEffectsAndResults(defender, offender);
        }

        cleanupMove(offender);
        for (FighterInfo defender : defenders)
            cleanupMove(defender);

        CombatAttribute.INSTANCE.set(offender.entity(), offender.com);
        for (FighterInfo defender : defenders)
            CombatAttribute.INSTANCE.set(defender.entity(), defender.com);
    }

    private static void applyScores(FighterInfo self) {
        if (self.act.action.requiresNoRoll())
            return;

        self.act.resetScore();
        self.act.scorePlayerMod = self.mod.roll + self.mod.metaRoll;
        self.act.critical = Dices.critical();
        if (self.act.critical == Criticalness.FAIL)
            self.act.successful = false;

        applyBuffs(self.com.buffs, Effect.Stage.PRE_SCORE, self, null);
        applyEffects(self.passiveEffects(), Effect.Stage.PRE_SCORE, self, null);
        applyEffects(self.act.action.self, Effect.Stage.SCORE, self, null);
        applyBuffs(self.com.buffs, Effect.Stage.SCORE, self, null);
        applyEffects(self.passiveEffects(), Effect.Stage.SCORE, self, null);
    }

    private static void applyEffectsAndResults(FighterInfo winner, FighterInfo looser) {
        applyEffects(winner.act.action.self, Effect.Stage.ACTION, winner, looser);
        applyEffects(winner.act.action.target, Effect.Stage.ACTION, looser, winner);

        applyBuffs(winner.com.buffs, Effect.Stage.ACTION, winner, looser);
        applyBuffs(looser.com.buffs, Effect.Stage.ACTION, looser, winner);
        applyEffects(winner.passiveEffects(), Effect.Stage.ACTION, winner, looser);
        applyEffects(looser.passiveEffects(), Effect.Stage.ACTION, looser, winner);

        if (winner.act.action.isOffencive() && !winner.act.action.requiresNoRoll()) {
            final Combo.ComboLookup combo = Combo.find(ActionRegistry.getCombos(), winner.com.prevActions, winner.act.action.id);

            if (combo != null) {
                winner.com.prevActions.removeAll(combo.match);
                winner.comboAction = combo.c.action;
                applyEffects(combo.c.action.self, Effect.Stage.ACTION, winner, looser);
                applyEffects(combo.c.action.target, Effect.Stage.ACTION, looser, winner);
            } else {
                winner.com.prevActions.add(winner.act.action.id);
            }
        }

        applyActionResults(winner);
        applyActionResults(looser);

        CombatNotifications.actionResult(winner, looser);
    }

    private void finishSoloMove(FighterInfo self) {
        self.com.phase = CombatContext.Phase.END;

        if (self.act.action.hasAnyTag(ActionTag.apply)) {
            final EntityLivingBase entity = self.entity();
            final ItemStack heldItem = entity.getHeldItem();
            if (heldItem != null && heldItem.stackSize > 0) {
                final List<Buff> newBuffs = PotionsHandler.convertItemStack(heldItem, entity);
                if (!newBuffs.isEmpty()) {
                    self.act.buffsToReceive.addAll(newBuffs);
                    heldItem.stackSize--;

                    if (heldItem.stackSize == 0 && entity instanceof EntityPlayer) {
                        final EntityPlayer player = ((EntityPlayer) entity);
                        player.inventory.mainInventory[player.inventory.currentItem] = null;
                        player.inventory.markDirty();
                        MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(player, heldItem));
                    }
                }
            }
        }

        applyEffects(self.act.action.self, Effect.Stage.ACTION, self, null);
        applyBuffs(self.com.buffs, Effect.Stage.ACTION, self, null);
        applyEffects(self.passiveEffects(), Effect.Stage.ACTION, self, null);
        applyActionResults(self);

        CombatNotifications.soloMoveResult(self);

        cleanupMove(self);
        CombatAttribute.INSTANCE.set(self.entity(), self.com);
    }

    private static void cleanupMove(FighterInfo self) {
        self.com.removeEndedEffects();
        self.com.softReset();
        ActionAttribute.remove(self.entity());
    }

    private static void applyBuffs(List<Buff> buffs, Effect.Stage stage, FighterInfo self, FighterInfo other) {
        for (Buff b : buffs)
            if (b.shouldApply(stage, self, other))
                b.applyEffect(stage, self, other);
    }

    private static void applyEffects(List<Effect> effects, Effect.Stage stage, FighterInfo self, FighterInfo other) {
        for (Effect e : effects)
            if (e.shouldApply(stage, self, other))
                e.apply(stage, self, other);
    }

    private static void applyActionResults(FighterInfo self) {
        final EntityLivingBase selfEntity = self.entity();

        final CombatDefines.DamageSettings damageSettings = More.DEFINES.combat().damageSettings;
        final int armorAmount = self.chr.armor > 0 ? self.chr.armor : selfEntity.getTotalArmorValue();
        final int MIN_DAMAGE = 1;

        float totalDamage = 0;
        for (DamageAmount da : self.com.damageToReceive) {
            totalDamage += da.piecing
                    ? da.amount
                    : damageSettings.applyArmor(da.amount, armorAmount, self.chr.damageThreshold);
        }

        if (selfEntity instanceof EntityPlayer)
            ((EntityPlayer) selfEntity).inventory.damageArmor(totalDamage);

        if (totalDamage > MIN_DAMAGE)
            self.com.prevActions.clear();

        for (Buff buff : self.act.buffsToReceive)
            Buff.mergeBuff(self.com.buffs, buff);

        final boolean deadlyAttack = totalDamage > 0 && selfEntity.getHealth() - totalDamage <= MIN_DAMAGE;
        final float currentHealth = selfEntity.getHealth();
        final float newHealth;

        if (deadlyAttack) {
            if (self.com.knockedOut) {
                newHealth = self.com.isTraining()
                        ? self.com.healthBeforeJoin
                        : 0;
                self.com.knockedOut = false;
                CombatNotifications.notifyKilled(self);
            } else {
                newHealth = MIN_DAMAGE;
                self.com.knockedOut = true;
                CombatNotifications.notifyKnockedOut(self);
            }
        } else if (totalDamage >= MIN_DAMAGE) {
            newHealth = currentHealth - totalDamage;
        } else {
            newHealth = currentHealth;
        }

        if (newHealth != currentHealth) {
            selfEntity.setHealth(newHealth);
            selfEntity.attackEntityFrom(DamageSource.generic, 0); // Visual damage

            if (newHealth <= 0)
                selfEntity.onDeath(new DamageSource("CRAbS"));
        }
    }

    @SubscribeEvent
    public void onLivingJoinWorld(EntityJoinWorldEvent event) {
        if (event.entity.worldObj.isRemote || !(event.entity instanceof EntityLivingBase))
            return;

        CombatAttribute.get((EntityLivingBase) event.entity)
                .filter(com -> com.phase.isInCombat())
                .ifPresent(com -> resetCombatantWithRelatives((EntityLivingBase) event.entity));
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player.worldObj.isRemote)
            return;

        resetCombatantWithRelatives(event.player);
    }

    public void joinCombat(EntityLivingBase target, CombatContext com) {
        com.phase = CombatContext.Phase.IDLE;

        CombatAttribute.INSTANCE.set(target, com);
        ActionAttribute.remove(target);
    }

    public void removeFromCombat(EntityLivingBase entity, CombatContext com) {
        resetCombatantWithRelatives(entity);

        if (com.isTraining())
            entity.setHealth(com.healthBeforeJoin);

        com.hardReset();
        CombatAttribute.INSTANCE.set(entity, com);
        ActionAttribute.remove(entity);
    }

    public static void resetCombatantWithRelatives(EntityLivingBase entity) {
        final CombatContext com = CombatAttribute.get(entity).orElse(null);
        if (com == null)
            return;

        final Stream<Integer> relatives = Stream.concat(Stream.of(com.offender), com.defenders.stream());

        ActionAttribute.remove(entity);
        CombatAttribute.INSTANCE.update(entity, CombatContext::softReset);

        relatives.map(id -> GetUtils.entityLiving(entity, id))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(CombatManager::resetCombatantWithRelatives);
    }
}
