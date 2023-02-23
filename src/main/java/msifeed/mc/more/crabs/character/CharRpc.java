package msifeed.mc.more.crabs.character;

import cpw.mods.fml.client.FMLClientHandler;
import msifeed.mc.Bootstrap;
import msifeed.mc.commons.logs.ExternalLogs;
import msifeed.mc.commons.traits.Trait;
import msifeed.mc.extensions.chat.SpeechatRpc;
import msifeed.mc.extensions.chat.formatter.MiscFormatter;
import msifeed.mc.extensions.tweaks.HealthModifier;
import msifeed.mc.more.More;
import msifeed.mc.more.crabs.rolls.Modifiers;
import msifeed.mc.more.crabs.rolls.Rolls;
import msifeed.mc.more.crabs.utils.CharacterAttribute;
import msifeed.mc.more.crabs.utils.Differ;
import msifeed.mc.more.crabs.utils.GetUtils;
import msifeed.mc.more.crabs.utils.MetaAttribute;
import msifeed.mc.sys.attributes.MissingRequiredAttributeException;
import msifeed.mc.sys.rpc.RpcContext;
import msifeed.mc.sys.rpc.RpcException;
import msifeed.mc.sys.rpc.RpcMethodHandler;
import msifeed.mc.sys.utils.ChatUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IChatComponent;

public enum CharRpc {
    INSTANCE;

    private static final String updateChar = Bootstrap.MODID + ":char.update";
    private static final String refreshName = Bootstrap.MODID + ":char.refreshName";
    private static final String clearEntity = Bootstrap.MODID + ":char.clear";
    private static final String rollAbility = Bootstrap.MODID + ":char.roll";

    public static void updateChar(int entityId, Character character) {
        More.RPC.sendToServer(updateChar, entityId, character.toNBT());
    }

    public static void clearEntity(int entityId) {
        More.RPC.sendToServer(clearEntity, entityId);
    }

    @RpcMethodHandler(updateChar)
    public void onUpdateChar(RpcContext ctx, int entityId, NBTTagCompound charNbt) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId).orElse(null);
        if (target == null)
            return;

        final boolean senderIsGm = CharacterAttribute.has(sender, Trait.gm);
        final boolean senderIsTarget = sender.getEntityId() == target.getEntityId();
        final boolean targetIsPlayer = target instanceof EntityPlayer;

        if (!senderIsGm && !senderIsTarget && targetIsPlayer) {
            ExternalLogs.log(sender, "warning", String.format("%s tried to change character %s while not GM!", sender.getCommandSenderName(), target.getCommandSenderName()));
            return;
        }

        final Character after = CharacterAttribute.get(target).orElse(null);
        if (after != null) {
            final Character before = new Character(after);
            CharacterAttribute.INSTANCE.update(target, c -> c.fromNBT(charNbt));

            Differ.printDiffs(sender, target, before, after);

            if (target instanceof EntityPlayer) {
                if (!before.name.equals(after.name)) {
                    ((EntityPlayer) target).refreshDisplayName();
                    More.RPC.sendToAll(refreshName, entityId);
                }

                if (before.countMaxHealth() != after.countMaxHealth()) {
                    HealthModifier.applyModifier((EntityPlayer) target, after);
                }
            }
        } else {
            final Character c = new Character();
            c.fromNBT(charNbt);
            CharacterAttribute.INSTANCE.set(target, c);
        }
    }

    @RpcMethodHandler(refreshName)
    public void onRefreshName(RpcContext ctx, int entityId) {
        final Entity entity = FMLClientHandler.instance().getWorldClient().getEntityByID(entityId);
        if (entity instanceof EntityPlayer)
            ((EntityPlayer) entity).refreshDisplayName();
    }

    @RpcMethodHandler(clearEntity)
    public void onClearEntity(RpcContext ctx, int entityId) {
        final EntityPlayerMP sender = ctx.getServerHandler().playerEntity;
        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .filter(e -> !(e instanceof EntityPlayer))
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        if (CharacterAttribute.require(sender).has(Trait.gm)) {
            CharacterAttribute.INSTANCE.set(target, null);
            MetaAttribute.INSTANCE.set(target, null);
        }
    }

    public static void rollAbility(int entityId, Ability ability) {
        More.RPC.sendToServer(rollAbility, entityId, ability.ordinal());
    }

    @RpcMethodHandler(rollAbility)
    public void onRollAbility(RpcContext ctx, int entityId, int abilityOrd) {
        final EntityPlayer sender = ctx.getServerHandler().playerEntity;
        final EntityLivingBase target = GetUtils.entityLiving(sender, entityId)
                .orElseThrow(() -> new RpcException(sender, "invalid target entity"));

        final Ability[] abilityValues = Ability.values();
        if (abilityOrd >= abilityValues.length)
            return;

        try {
            final Character c = CharacterAttribute.require(target);
            final Modifiers m = MetaAttribute.require(target).modifiers;
            final Ability a = abilityValues[abilityOrd];

            final Rolls.Result roll = Rolls.rollAbility(c, m, a);
            final String name = ChatUtils.getPrettyName(target);
            final int range = More.DEFINES.get().chat.rollRadius;
            final IChatComponent cc = MiscFormatter.formatAbilityRoll(name, a, m, roll);
            SpeechatRpc.sendRaw(target, range, cc);
            ExternalLogs.log(sender, "roll", cc.getUnformattedText());
        } catch (MissingRequiredAttributeException e) {
            throw new RpcException(sender, "target has no character property");
        }
    }
}
