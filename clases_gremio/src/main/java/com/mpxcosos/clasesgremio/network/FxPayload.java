package com.mpxcosos.clasesgremio.network;

import com.mpxcosos.clasesgremio.ClasesGremio;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Servidor -> cliente: dibujar un efecto de fuego.
 * kind: ver constantes. entityId = -1 si es en una posición fija.
 * a, b, c: parámetros según el tipo (radio, duración, retraso, etc.).
 */
public record FxPayload(int kind, int entityId, double x, double y, double z, float a, float b, float c)
        implements CustomPacketPayload {
    public static final int TRAIL = 1;    // a = duración (ticks), b = ancho
    public static final int RING = 2;     // a = radio final, b = duración, c = retraso
    public static final int PILLAR = 3;   // a = altura, b = duración, c = retraso
    public static final int AURA = 4;     // a = duración
    public static final int XSLASH = 5;   // a = yaw del atacante, b = tamaño
    public static final int SHAKE = 6;    // a = intensidad, b = radio
    public static final int BURST = 7;    // a = tamaño
    public static final int SLAM_ARC = 8; // a = yaw, b = radio

    public static final Type<FxPayload> TYPE = new Type<>(ClasesGremio.id("fx"));
    public static final StreamCodec<ByteBuf, FxPayload> STREAM_CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeByte(p.kind);
                buf.writeInt(p.entityId);
                buf.writeDouble(p.x);
                buf.writeDouble(p.y);
                buf.writeDouble(p.z);
                buf.writeFloat(p.a);
                buf.writeFloat(p.b);
                buf.writeFloat(p.c);
            },
            buf -> new FxPayload(buf.readByte(), buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
