package com.mpxcosos.clasesgremio.effect;

import com.mpxcosos.clasesgremio.registry.ModDamage;
import com.mpxcosos.clasesgremio.registry.ModParticles;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Efecto propio del gremio. Si tiene tipo de daño, hace daño cada cierto intervalo (sangrado, quemadura).
 */
public class GremioEffect extends MobEffect {
    @Nullable private final ResourceKey<DamageType> damageType;
    private final int interval;
    private final float damage;
    private final boolean embers;

    public GremioEffect(MobEffectCategory category, int color) {
        this(category, color, null, 0, 0f, false);
    }

    public GremioEffect(MobEffectCategory category, int color, @Nullable ResourceKey<DamageType> damageType,
                        int interval, float damage, boolean embers) {
        super(category, color);
        this.damageType = damageType;
        this.interval = interval;
        this.damage = damage;
        this.embers = embers;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return damageType != null && interval > 0 && duration % interval == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (damageType != null && entity.level() instanceof ServerLevel level) {
            entity.invulnerableTime = 0;
            entity.hurt(ModDamage.source(level, damageType, null), damage * (amplifier + 1));
            if (embers) {
                level.sendParticles(ModParticles.BRASA.get(), entity.getX(), entity.getY(0.5), entity.getZ(),
                        6, 0.25, 0.4, 0.25, 0.02);
            }
        }
        return true;
    }
}
