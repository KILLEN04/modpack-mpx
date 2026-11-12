package com.mpxcosos.clasesgremio.data;

import com.mpxcosos.clasesgremio.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/** Reglas comunes: qué clase tiene el jugador y si su rango le alcanza para una etapa. */
public final class GuildGate {
    public static final GuildRank[] RANGO_MINIMO = {GuildRank.F, GuildRank.D, GuildRank.B, GuildRank.S};

    @Nullable
    public static GuildClass classOf(Player player) {
        return GuildClass.byId(player.getData(ModAttachments.GUILD_DATA).classId());
    }

    /** Etapa (0-3) que permite el rango del jugador. */
    public static int stageOf(Player player) {
        return GuildRank.byIndex(player.getData(ModAttachments.GUILD_DATA).rank()).armorStage();
    }

    public static boolean is(Player player, GuildClass c) {
        return classOf(player) == c;
    }

    /** null si puede usar algo de esa clase y etapa; si no, el motivo. */
    @Nullable
    public static Component denyReason(Player player, GuildClass c, int stage, String kind) {
        if (player.isCreative()) return null;
        if (classOf(player) != c)
            return Component.translatable("msg.clases_gremio." + kind + "_clase", c.displayName());
        if (stageOf(player) < stage)
            return Component.translatable("msg.clases_gremio." + kind + "_rango", RANGO_MINIMO[stage].letter());
        return null;
    }

    private GuildGate() {
    }
}
