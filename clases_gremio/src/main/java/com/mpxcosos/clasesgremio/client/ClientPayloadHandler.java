package com.mpxcosos.clasesgremio.client;

import com.mpxcosos.clasesgremio.registry.ModAttachments;
import com.mpxcosos.clasesgremio.network.OpenRecepcionistaPayload;
import com.mpxcosos.clasesgremio.network.SyncGuildDataPayload;
import net.minecraft.client.Minecraft;

/** Solo cliente. Se llama desde los handlers registrados en ModNetwork. */
public final class ClientPayloadHandler {
    public static void handleSync(SyncGuildDataPayload payload) {
        ClientGuildData.set(payload.data());
        // copia en el jugador local para que los ítems (p. ej. arcos) puedan leer la clase
        if (Minecraft.getInstance().player != null)
            Minecraft.getInstance().player.setData(ModAttachments.GUILD_DATA, payload.data());
    }

    public static void handleCooldown(com.mpxcosos.clasesgremio.network.CooldownPayload payload) {
        ClientAbilities.setCooldown(payload);
    }

    public static void handleFx(com.mpxcosos.clasesgremio.network.FxPayload payload) {
        com.mpxcosos.clasesgremio.client.fx.FireFx.handle(payload);
    }

    public static void handleOpen(OpenRecepcionistaPayload payload) {
        Minecraft.getInstance().setScreen(new RecepcionistaScreen(payload.entityId()));
    }

    private ClientPayloadHandler() {
    }
}
