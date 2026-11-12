package com.mpxcosos.clasesgremio.ability;

import com.mpxcosos.clasesgremio.data.GuildAbility;
import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.data.GuildGate;
import com.mpxcosos.clasesgremio.registry.ModDamage;
import com.mpxcosos.clasesgremio.registry.ModEffects;
import com.mpxcosos.clasesgremio.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Habilidades y pasivas del Guerrero (servidor). */
public final class GuerreroHabilidades {
    private static final ResourceLocation VIDA = com.mpxcosos.clasesgremio.ClasesGremio.id("guerrero_vida");
    private static final ResourceLocation FIRMEZA = com.mpxcosos.clasesgremio.ClasesGremio.id("guerrero_firmeza");

    static boolean use(ServerPlayer p, GuildAbility a, AbilityManager.State s) {
        ServerLevel level = p.serverLevel();
        return switch (a) {
            case CARGA -> carga(p, level, s);
            case GRITO -> grito(p, level);
            case SISMICO -> sismico(p, level, s);
            case FURIA -> furia(p, level);
        };
    }

    private static void sound(ServerLevel level, Vec3 pos, SoundEvent ev, float vol, float pitch) {
        level.playSound(null, pos.x, pos.y, pos.z, ev, SoundSource.PLAYERS, vol, pitch);
    }

    private static boolean enemyOf(Player p, LivingEntity e) {
        if (e == p || !e.isAlive() || e.isSpectator()) return false;
        if (e instanceof OwnableEntity own && own.getOwner() == p) return false;
        return !p.isAlliedTo(e);
    }

    // ------------------------------------------------------------------ Carga
    private static boolean carga(ServerPlayer p, ServerLevel level, AbilityManager.State s) {
        s.chargeTicks = 9;
        s.chargeHit.clear();
        Fx.trail(p, 12, 1.1f);
        Fx.burst(level, p.position().add(0, 0.8, 0), 0.8f);
        sound(level, p.position(), ModSounds.CARGA.get(), 1.2f, 1f);
        return true;
    }

    // ------------------------------------------------------------------ Grito de guerra
    private static boolean grito(ServerPlayer p, ServerLevel level) {
        Vec3 pos = p.position();
        for (Mob m : level.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(12))) {
            if (!enemyOf(p, m)) continue;
            m.setTarget(p);
            if (m instanceof Enemy && m.distanceToSqr(p) < 36) {
                m.addEffect(new MobEffectInstance(ModEffects.ARMADURA_ROTA, 120, 0), p);
                m.addEffect(new MobEffectInstance(ModEffects.QUEMADURA, 60, 0), p);
            }
        }
        p.addEffect(new MobEffectInstance(ModEffects.BALUARTE, 160, 0, false, true, true));
        Fx.ring(level, pos.add(0, 0.05, 0), 7f, 14, 0);
        Fx.ring(level, pos.add(0, 0.05, 0), 11f, 18, 4);
        for (int i = 0; i < 8; i++) {
            double ang = i * Math.PI / 4;
            Fx.pillar(level, pos.add(Math.cos(ang) * 2.6, 0, Math.sin(ang) * 2.6), 3.2f, 18, i % 3);
        }
        Fx.aura(p, 160);
        Fx.embers(level, pos.add(0, 1, 0), 60, 1.2, 0.12);
        sound(level, pos, ModSounds.GRITO.get(), 2.0f, 0.95f);
        return true;
    }

    // ------------------------------------------------------------------ Golpe sísmico
    private static boolean sismico(ServerPlayer p, ServerLevel level, AbilityManager.State s) {
        Vec3 look = p.getLookAngle();
        if (p.onGround()) {
            p.setDeltaMovement(look.x * 0.45, 0.95, look.z * 0.45);
        } else {
            p.setDeltaMovement(p.getDeltaMovement().x * 0.3, -1.8, p.getDeltaMovement().z * 0.3);
        }
        p.hurtMarked = true;
        s.slamTicks = 1;
        Fx.trail(p, 10, 0.9f);
        sound(level, p.position(), ModSounds.CARGA.get(), 0.8f, 0.7f);
        return true;
    }

    private static void slam(ServerPlayer p, ServerLevel level) {
        Vec3 pos = p.position();
        float dmg = 10f;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(5.5, 2.5, 5.5))) {
            if (!enemyOf(p, e)) continue;
            Vec3 away = e.position().subtract(pos).multiply(1, 0, 1);
            double d = Math.max(0.5, away.length());
            away = away.normalize();
            e.hurt(ModDamage.source(level, ModDamage.GUERRERO, p), dmg * (float) Math.max(0.5, 1 - d / 8));
            e.push(away.x * 1.1, 0.75, away.z * 1.1);
            e.hurtMarked = true;
            e.addEffect(new MobEffectInstance(ModEffects.QUEMADURA, 80, 0), p);
            e.addEffect(new MobEffectInstance(ModEffects.ARMADURA_ROTA, 80, 0), p);
        }
        Fx.slamArc(p, 2.4f);
        Fx.ring(level, pos.add(0, 0.05, 0), 5f, 12, 0);
        Fx.ring(level, pos.add(0, 0.05, 0), 8f, 16, 3);
        Fx.ring(level, pos.add(0, 0.05, 0), 11f, 20, 6);
        for (int i = 0; i < 10; i++) {
            double ang = level.random.nextDouble() * Math.PI * 2;
            double r = 2 + level.random.nextDouble() * 3.5;
            Fx.pillar(level, pos.add(Math.cos(ang) * r, 0, Math.sin(ang) * r), 2.5f + level.random.nextFloat() * 2f, 16, 2 + i % 4);
        }
        Fx.shake(level, pos, 1.0f, 18f);
        Fx.embers(level, pos.add(0, 0.5, 0), 120, 2.5, 0.2);
        sound(level, pos, ModSounds.SISMICO.get(), 2.2f, 1f);
    }

    // ------------------------------------------------------------------ Furia
    private static boolean furia(ServerPlayer p, ServerLevel level) {
        Vec3 pos = p.position();
        p.addEffect(new MobEffectInstance(ModEffects.FURIA, 240, 0, false, true, true));
        Fx.ring(level, pos.add(0, 0.05, 0), 6f, 12, 0);
        Fx.pillar(level, pos, 5.5f, 20, 0);
        Fx.aura(p, 240);
        Fx.burst(level, pos.add(0, 1, 0), 1.6f);
        Fx.embers(level, pos.add(0, 1, 0), 80, 1.0, 0.15);
        sound(level, pos, ModSounds.FURIA.get(), 2.0f, 1f);
        p.displayClientMessage(Component.translatable("msg.clases_gremio.furia").withStyle(ChatFormatting.GOLD), true);
        return true;
    }

    // ------------------------------------------------------------------ cada tick (servidor)
    public static void tick(ServerPlayer p) {
        AbilityManager.State s = AbilityManager.state(p);
        ServerLevel level = p.serverLevel();
        if (s.chargeTicks > 0) {
            s.chargeTicks--;
            Vec3 look = p.getLookAngle();
            Vec3 dir = new Vec3(look.x, 0, look.z);
            dir = dir.lengthSqr() < 1e-4 ? Vec3.ZERO : dir.normalize();
            double vy = p.onGround() ? 0.08 : Math.min(p.getDeltaMovement().y, 0.1);
            p.setDeltaMovement(dir.x * 1.3, vy, dir.z * 1.3);
            p.hurtMarked = true;
            p.fallDistance = 0;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(1.3))) {
                if (!enemyOf(p, e) || !s.chargeHit.add(e.getId())) continue;
                e.hurt(ModDamage.source(level, ModDamage.GUERRERO, p), 7f);
                e.push(dir.x * 1.5, 0.5, dir.z * 1.5);
                e.hurtMarked = true;
                e.addEffect(new MobEffectInstance(ModEffects.QUEMADURA, 60, 0), p);
                Fx.burst(level, e.position().add(0, e.getBbHeight() * 0.5, 0), 1.0f);
                sound(level, e.position(), ModSounds.SISMICO.get(), 0.6f, 1.6f);
            }
        }
        if (s.slamTicks > 0) {
            s.slamTicks++;
            p.fallDistance = 0;
            if (s.slamTicks > 9 && !p.onGround() && p.getDeltaMovement().y < 0.25) {
                p.setDeltaMovement(p.getDeltaMovement().x, -1.6, p.getDeltaMovement().z);
                p.hurtMarked = true;
            }
            if ((s.slamTicks > 4 && p.onGround()) || s.slamTicks > 40) {
                s.slamTicks = 0;
                slam(p, level);
            }
        }
        if (p.tickCount % 20 == 0) applyPassives(p);
    }

    // ------------------------------------------------------------------ pasivas
    public static void applyPassives(ServerPlayer p) {
        boolean g = GuildGate.is(p, GuildClass.GUERRERO);
        int stage = GuildGate.stageOf(p);
        modifier(p.getAttribute(Attributes.MAX_HEALTH), VIDA, 4.0, g);
        modifier(p.getAttribute(Attributes.KNOCKBACK_RESISTANCE), FIRMEZA, 0.3, g && stage >= 1);
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    private static void modifier(AttributeInstance inst, ResourceLocation id, double amount, boolean want) {
        if (inst == null) return;
        boolean has = inst.hasModifier(id);
        if (want && !has) inst.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        else if (!want && has) inst.removeModifier(id);
    }

    /** Bloquear con escudo cura (B-A). */
    public static void onShieldBlock(ServerPlayer p) {
        if (!GuildGate.is(p, GuildClass.GUERRERO) || GuildGate.stageOf(p) < 2) return;
        AbilityManager.State s = AbilityManager.state(p);
        long now = p.level().getGameTime();
        if (now < s.shieldHealReady) return;
        s.shieldHealReady = now + 15;
        p.heal(2.0f);
        Fx.embers(p.serverLevel(), p.position().add(0, 1, 0), 10, 0.4, 0.05);
    }

    /** Último aliento (S): evita una muerte por minuto. @return true si salvó al jugador. */
    public static boolean onDeath(ServerPlayer p) {
        if (!GuildGate.is(p, GuildClass.GUERRERO) || GuildGate.stageOf(p) < 3) return false;
        AbilityManager.State s = AbilityManager.state(p);
        long now = p.level().getGameTime();
        if (now < s.alientoReady) return false;
        s.alientoReady = now + 1200;
        ServerLevel level = p.serverLevel();
        p.setHealth(6f);
        p.addEffect(new MobEffectInstance(ModEffects.BALUARTE, 60, 0, false, true, true));
        Fx.ring(level, p.position().add(0, 0.05, 0), 6f, 14, 0);
        Fx.pillar(level, p.position(), 4f, 18, 0);
        Fx.aura(p, 60);
        sound(level, p.position(), ModSounds.ULTIMO_ALIENTO.get(), 1.6f, 1f);
        p.displayClientMessage(Component.translatable("msg.clases_gremio.ultimo_aliento").withStyle(ChatFormatting.GOLD), true);
        return true;
    }

    private GuerreroHabilidades() {
    }
}
