package com.mpxcosos.clasesgremio.network;

import com.mpxcosos.clasesgremio.ClasesGremio;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Servidor -> cliente: abre el menú de la recepcionista con ese id de entidad. */
public record OpenRecepcionistaPayload(int entityId) implements CustomPacketPayload {
    public static final Type<OpenRecepcionistaPayload> TYPE = new Type<>(ClasesGremio.id("open_recepcionista"));
    public static final StreamCodec<ByteBuf, OpenRecepcionistaPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(OpenRecepcionistaPayload::new, OpenRecepcionistaPayload::entityId);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
