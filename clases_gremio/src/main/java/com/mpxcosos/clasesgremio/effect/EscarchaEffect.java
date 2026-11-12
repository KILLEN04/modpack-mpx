package com.mpxcosos.clasesgremio.effect;

import com.mpxcosos.clasesgremio.ClasesGremio;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Escarcha: el hielo del arco entumece al objetivo (-15 % de velocidad por nivel). */
public class EscarchaEffect extends MobEffect {
    public EscarchaEffect() {
        super(MobEffectCategory.HARMFUL, 0x7FC8F0);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, ClasesGremio.id("escarcha"), -0.15,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
