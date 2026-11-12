package com.mpxcosos.clasesgremio.network;

import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.data.PlayerGuildData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Servidor -> cliente: copia de la clase y el rango del jugador. */
public record SyncGuildDataPayload(PlayerGuildData data) implements CustomPacketPayload {
    public static final Type<SyncGuildDataPayload> TYPE = new Type<>(ClasesGremio.id("sync_guild_data"));
    public static final StreamCodec<ByteBuf, SyncGuildDataPayload> STREAM_CODEC =
            PlayerGuildData.STREAM_CODEC.map(SyncGuildDataPayload::new, SyncGuildDataPayload::data);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
