package com.mpxcosos.clasesgremio.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

/**
 * Datos del gremio guardados en cada jugador.
 *
 * @param classId  id de la clase ("" si aún no eligió)
 * @param rank     índice del rango (0 = F ... 6 = S)
 * @param progress progreso de la misión de ascenso actual
 */
public record PlayerGuildData(String classId, int rank, int progress) {
    public static final PlayerGuildData EMPTY = new PlayerGuildData("", 0, 0);

    public static final Codec<PlayerGuildData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.optionalFieldOf("clase", "").forGetter(PlayerGuildData::classId),
            Codec.INT.optionalFieldOf("rango", 0).forGetter(PlayerGuildData::rank),
            Codec.INT.optionalFieldOf("progreso", 0).forGetter(PlayerGuildData::progress)
    ).apply(i, PlayerGuildData::new));

    public static final StreamCodec<ByteBuf, PlayerGuildData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayerGuildData::classId,
            ByteBufCodecs.VAR_INT, PlayerGuildData::rank,
            ByteBufCodecs.VAR_INT, PlayerGuildData::progress,
            PlayerGuildData::new);

    public boolean hasClass() {
        return guildClass() != null;
    }

    @Nullable
    public GuildClass guildClass() {
        return GuildClass.byId(classId);
    }

    public GuildRank guildRank() {
        return GuildRank.byIndex(rank);
    }

    public PlayerGuildData withClass(@Nullable GuildClass c) {
        return new PlayerGuildData(c == null ? "" : c.id(), c == null ? 0 : rank, 0);
    }

    public PlayerGuildData withRank(GuildRank r) {
        return new PlayerGuildData(classId, r.ordinal(), 0);
    }
}
