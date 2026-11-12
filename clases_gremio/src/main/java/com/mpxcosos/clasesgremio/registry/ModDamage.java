package com.mpxcosos.clasesgremio.registry;

import com.mpxcosos.clasesgremio.ClasesGremio;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** Tipos de daño propios (data/clases_gremio/damage_type/*.json). */
public final class ModDamage {
    public static final ResourceKey<DamageType> TORMENTA = key("tormenta");
    public static final ResourceKey<DamageType> SANGRADO = key("sangrado");
    public static final ResourceKey<DamageType> QUEMADURA = key("quemadura");
    public static final ResourceKey<DamageType> GUERRERO = key("guerrero");

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ClasesGremio.id(name));
    }

    public static DamageSource source(Level level, ResourceKey<DamageType> type, @Nullable Entity attacker) {
        var holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
        return attacker != null ? new DamageSource(holder, attacker) : new DamageSource(holder);
    }

    private ModDamage() {
    }
}
