package com.mpxcosos.clasesgremio.registry;

import com.mpxcosos.clasesgremio.ClasesGremio;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Partículas propias del gremio (texturas en assets/clases_gremio/textures/particle). */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ClasesGremio.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VIENTO = reg("viento");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ESCARCHA = reg("escarcha");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> CHISPA = reg("chispa");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ESTRELLA = reg("estrella");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> RAYO = reg("rayo");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BRASA = reg("brasa");

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> reg(String name) {
        return PARTICLES.register(name, () -> new SimpleParticleType(true) {});
    }

    private ModParticles() {
    }
}
