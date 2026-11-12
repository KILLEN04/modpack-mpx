package com.mpxcosos.clasesgremio.network;

import com.mpxcosos.clasesgremio.ClasesGremio;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Servidor -> cliente: recarga de una habilidad (ticks restantes y total). */
public record CooldownPayload(int slot, int remaining, int total) implements CustomPacketPayload {
    public static final Type<CooldownPayload> TYPE = new Type<>(ClasesGremio.id("cooldown"));
    public static final StreamCodec<ByteBuf, CooldownPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CooldownPayload::slot,
            ByteBufCodecs.VAR_INT, CooldownPayload::remaining,
            ByteBufCodecs.VAR_INT, CooldownPayload::total,
            CooldownPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
