package com.mpxcosos.clasesgremio.network;

import com.mpxcosos.clasesgremio.ClasesGremio;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente -> servidor: usar la habilidad del slot (0-3). */
public record UseAbilityPayload(int slot) implements CustomPacketPayload {
    public static final Type<UseAbilityPayload> TYPE = new Type<>(ClasesGremio.id("use_ability"));
    public static final StreamCodec<ByteBuf, UseAbilityPayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(UseAbilityPayload::new, UseAbilityPayload::slot);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
