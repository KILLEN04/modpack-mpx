package com.mpxcosos.clasesgremio.event;

import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.item.ArcoGremioItem;
import com.mpxcosos.clasesgremio.registry.ModEffects;
import com.mpxcosos.clasesgremio.registry.ModParticles;
import com.mpxcosos.clasesgremio.registry.ModSounds;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Poderes elementales de las flechas de los arcos del gremio.
 * Todo es propio del mod: partículas, sonidos, efecto Escarcha y la descarga de tormenta.
 */
public final class ArcoEventos {
    /** Etapa del arco guardada en la flecha (1 viento, 2 hielo, 3 tormenta). */
    private static final String TAG = "clases_gremio_elemento";
    private static final float PROB_DESCARGA = 0.25f;
    private static final float DANO_DESCARGA = 6f;
    private static final double RADIO_DESCARGA = 2.5;
    public static final ResourceKey<DamageType> TORMENTA =
            ResourceKey.create(Registries.DAMAGE_TYPE, ClasesGremio.id("tormenta"));

    public static void register() {
        NeoForge.EVENT_BUS.addListener(ArcoEventos::onJoin);
        NeoForge.EVENT_BUS.addListener(ArcoEventos::onArrowTick);
        NeoForge.EVENT_BUS.addListener(ArcoEventos::onDamage);
    }

    private static ArcoGremioItem bowOf(Player player) {
        for (ItemStack s : new ItemStack[]{player.getUseItem(), player.getMainHandItem(), player.getOffhandItem()}) {
            if (s.getItem() instanceof ArcoGremioItem bow) return bow;
        }
        return null;
    }

    private static void burst(ServerLevel level, ParticleOptions p, Vec3 pos, int n, double spread, double speed) {
        level.sendParticles(p, pos.x, pos.y, pos.z, n, spread, spread, spread, speed);
    }

    /** Al disparar: marca la flecha, le da velocidad (viento), más daño y su sonido. */
    private static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || event.loadedFromDisk()) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow) || !(arrow.getOwner() instanceof Player player)) return;
        ArcoGremioItem bow = bowOf(player);
        if (bow == null || bow.stage() == 0) return;
        CompoundTag data = arrow.getPersistentData();
        if (data.contains(TAG)) return;
        int stage = bow.stage();
        data.putInt(TAG, stage);
        arrow.setDeltaMovement(arrow.getDeltaMovement().scale(bow.speedMultiplier()));
        arrow.setBaseDamage(arrow.getBaseDamage() + 0.5 * stage);

        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ARCO_VIENTO.get(),
                SoundSource.PLAYERS, 0.8f, 0.9f + 0.1f * stage);
        if (stage >= 2) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.ARCO_DISPARO.get(),
                    SoundSource.PLAYERS, 0.6f, stage == 3 ? 0.8f : 1.1f);
        }
        burst(level, ModParticles.VIENTO.get(), player.getEyePosition(), 4 + 2 * stage, 0.25, 0.03);
    }

    /** Estela de partículas propias mientras la flecha vuela. */
    private static void onArrowTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow) || !(arrow.level() instanceof ServerLevel level)) return;
        int stage = arrow.getPersistentData().getInt(TAG);
        if (stage == 0 || arrow.getDeltaMovement().lengthSqr() < 0.04) return;
        Vec3 pos = arrow.position();
        if (arrow.tickCount % 2 == 0) burst(level, ModParticles.VIENTO.get(), pos, 1, 0.02, 0.0);
        if (stage >= 2 && arrow.tickCount % 2 == 1) burst(level, ModParticles.ESCARCHA.get(), pos, 1, 0.05, 0.0);
        if (stage >= 3) {
            burst(level, arrow.tickCount % 3 == 0 ? ModParticles.CHISPA.get() : ModParticles.ESTRELLA.get(), pos, 1, 0.08, 0.0);
        }
    }

    /** Al impactar: empuje (viento), Escarcha (hielo) y posible descarga (tormenta). */
    private static void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getDirectEntity() instanceof AbstractArrow arrow)) return;
        int stage = arrow.getPersistentData().getInt(TAG);
        if (stage == 0) return;
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel level)) return;
        Vec3 center = target.position().add(0, target.getBbHeight() * 0.5, 0);

        // Viento: empuje extra en la dirección de la flecha
        Vec3 dir = arrow.getDeltaMovement().multiply(1, 0, 1);
        if (dir.lengthSqr() > 1.0E-4) {
            Vec3 push = dir.normalize().scale(0.3 * stage);
            target.push(push.x, 0.08, push.z);
            target.hurtMarked = true;
        }
        burst(level, ModParticles.VIENTO.get(), center, 8, 0.35, 0.06);

        // Hielo: Escarcha (propia)
        if (stage >= 2) {
            target.addEffect(new MobEffectInstance(ModEffects.ESCARCHA, 60, stage - 1), arrow.getOwner());
            burst(level, ModParticles.ESCARCHA.get(), center, 14, 0.35, 0.03);
            level.playSound(null, target.getX(), target.getY(), target.getZ(), ModSounds.ARCO_HIELO.get(),
                    SoundSource.PLAYERS, 0.8f, 0.9f + level.random.nextFloat() * 0.2f);
        }

        // Tormenta: descarga propia
        if (stage >= 3 && level.random.nextFloat() < PROB_DESCARGA) {
            descarga(level, target, arrow.getOwner());
        }
    }

    /** Rayo hecho con partículas propias: no prende fuego ni daña al arquero. */
    private static void descarga(ServerLevel level, LivingEntity target, Entity owner) {
        Vec3 base = target.position();
        // zigzag desde el cielo hasta el objetivo
        double x = base.x, z = base.z;
        double top = base.y + 10;
        for (double y = top; y > base.y; y -= 0.35) {
            x += (level.random.nextDouble() - 0.5) * 0.45;
            z += (level.random.nextDouble() - 0.5) * 0.45;
            x = base.x + Math.max(-1.2, Math.min(1.2, x - base.x));
            z = base.z + Math.max(-1.2, Math.min(1.2, z - base.z));
            level.sendParticles(ModParticles.RAYO.get(), x, y, z, 1, 0, 0, 0, 0);
        }
        Vec3 hit = base.add(0, 0.2, 0);
        burst(level, ModParticles.CHISPA.get(), hit, 24, 0.6, 0.15);
        burst(level, ModParticles.ESTRELLA.get(), hit, 16, 0.8, 0.08);
        level.playSound(null, base.x, base.y, base.z, ModSounds.TORMENTA_TRUENO.get(), SoundSource.PLAYERS,
                2.0f, 0.9f + level.random.nextFloat() * 0.2f);

        DamageSource source = owner != null
                ? new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TORMENTA), owner)
                : new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(TORMENTA));
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(RADIO_DESCARGA))) {
            if (e == owner || !e.isAlive()) continue;
            e.invulnerableTime = 0;
            e.hurt(source, e == target ? DANO_DESCARGA : DANO_DESCARGA * 0.5f);
        }
    }

    private ArcoEventos() {
    }
}
