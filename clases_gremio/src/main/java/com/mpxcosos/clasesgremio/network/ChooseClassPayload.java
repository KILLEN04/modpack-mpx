package com.mpxcosos.clasesgremio.network;

import com.mpxcosos.clasesgremio.ClasesGremio;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Cliente -> servidor: el jugador confirmó una clase en el menú de la recepcionista. */
public record ChooseClassPayload(int entityId, String classId) implements CustomPacketPayload {
    public static final Type<ChooseClassPayload> TYPE = new Type<>(ClasesGremio.id("choose_class"));
    public static final StreamCodec<ByteBuf, ChooseClassPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ChooseClassPayload::entityId,
            ByteBufCodecs.STRING_UTF8, ChooseClassPayload::classId,
            ChooseClassPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
