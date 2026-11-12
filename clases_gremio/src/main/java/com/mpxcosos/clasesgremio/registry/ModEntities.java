package com.mpxcosos.clasesgremio.registry;

import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.entity.RecepcionistaEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, ClasesGremio.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RecepcionistaEntity>> RECEPCIONISTA =
            ENTITIES.register("recepcionista", () -> EntityType.Builder.of(RecepcionistaEntity::new, MobCategory.MISC)
                    .sized(0.6f, 2.0f)
                    .eyeHeight(1.75f)
                    .clientTrackingRange(10)
                    .build("recepcionista"));

    private ModEntities() {
    }
}
