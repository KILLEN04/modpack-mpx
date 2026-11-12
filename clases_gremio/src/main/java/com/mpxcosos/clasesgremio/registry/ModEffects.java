package com.mpxcosos.clasesgremio.registry;

import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.effect.EscarchaEffect;
import com.mpxcosos.clasesgremio.effect.GremioEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, ClasesGremio.MODID);

    public static final DeferredHolder<MobEffect, EscarchaEffect> ESCARCHA =
            EFFECTS.register("escarcha", EscarchaEffect::new);

    /** Sangrado: 1 de daño por segundo (por nivel). */
    public static final DeferredHolder<MobEffect, MobEffect> SANGRADO = EFFECTS.register("sangrado",
            () -> new GremioEffect(MobEffectCategory.HARMFUL, 0x9E1B1B, ModDamage.SANGRADO, 20, 1.0f, false));

    /** Quemadura: fuego del Guerrero, 1 de daño cada 3/4 s con brasas. */
    public static final DeferredHolder<MobEffect, MobEffect> QUEMADURA = EFFECTS.register("quemadura",
            () -> new GremioEffect(MobEffectCategory.HARMFUL, 0xFF6A1A, ModDamage.QUEMADURA, 15, 1.0f, true));

    /** Armadura rota: -25 % de armadura por nivel. */
    public static final DeferredHolder<MobEffect, MobEffect> ARMADURA_ROTA = EFFECTS.register("armadura_rota",
            () -> new GremioEffect(MobEffectCategory.HARMFUL, 0x6E6E78)
                    .addAttributeModifier(Attributes.ARMOR, ClasesGremio.id("armadura_rota"), -0.25,
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    /** Baluarte: recibe 35 % menos de daño (ver eventos del Guerrero). */
    public static final DeferredHolder<MobEffect, MobEffect> BALUARTE = EFFECTS.register("baluarte",
            () -> new GremioEffect(MobEffectCategory.BENEFICIAL, 0xC9A65A));

    /** Furia: +40 % de daño, +20 % de velocidad de ataque y robo de vida. */
    public static final DeferredHolder<MobEffect, MobEffect> FURIA = EFFECTS.register("furia",
            () -> new GremioEffect(MobEffectCategory.BENEFICIAL, 0xFF4A1A)
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE, ClasesGremio.id("furia_dano"), 0.4,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
                    .addAttributeModifier(Attributes.ATTACK_SPEED, ClasesGremio.id("furia_velocidad"), 0.2,
                            AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

    private ModEffects() {
    }
}
