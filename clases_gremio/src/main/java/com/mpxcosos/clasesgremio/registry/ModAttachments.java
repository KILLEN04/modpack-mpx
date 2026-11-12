package com.mpxcosos.clasesgremio.registry;

import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.data.PlayerGuildData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ClasesGremio.MODID);

    /** Clase, rango y progreso del jugador. Se guarda en el mundo y se conserva al morir. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerGuildData>> GUILD_DATA =
            ATTACHMENT_TYPES.register("guild_data", () -> AttachmentType.builder(() -> PlayerGuildData.EMPTY)
                    .serialize(PlayerGuildData.CODEC)
                    .copyOnDeath()
                    .build());

    private ModAttachments() {
    }
}
