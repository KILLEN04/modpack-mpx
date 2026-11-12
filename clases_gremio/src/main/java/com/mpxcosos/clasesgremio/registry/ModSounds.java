package com.mpxcosos.clasesgremio.registry;

import com.mpxcosos.clasesgremio.ClasesGremio;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Sonidos propios (assets/clases_gremio/sounds/*.ogg). */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, ClasesGremio.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ARCO_VIENTO = reg("arco_viento");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARCO_HIELO = reg("arco_hielo");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARCO_DISPARO = reg("arco_disparo");
    public static final DeferredHolder<SoundEvent, SoundEvent> TORMENTA_TRUENO = reg("tormenta_trueno");
    public static final DeferredHolder<SoundEvent, SoundEvent> ARMADURA_EQUIPAR = reg("armadura_equipar");
    public static final DeferredHolder<SoundEvent, SoundEvent> ESPADAZO = reg("espadazo");
    public static final DeferredHolder<SoundEvent, SoundEvent> CARGA = reg("carga");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRITO = reg("grito");
    public static final DeferredHolder<SoundEvent, SoundEvent> SISMICO = reg("sismico");
    public static final DeferredHolder<SoundEvent, SoundEvent> FURIA = reg("furia");
    public static final DeferredHolder<SoundEvent, SoundEvent> ULTIMO_ALIENTO = reg("ultimo_aliento");


    private static DeferredHolder<SoundEvent, SoundEvent> reg(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ClasesGremio.id(name)));
    }

    private ModSounds() {
    }
}
