package com.mpxcosos.clasesgremio.ability;

import com.mpxcosos.clasesgremio.data.GuildAbility;
import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.data.GuildGate;
import com.mpxcosos.clasesgremio.network.CooldownPayload;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Estado de habilidades por jugador (servidor): recargas y acciones en curso. */
public final class AbilityManager {
    public static final class State {
        public final long[] cooldownEnd = new long[4];
        public int chargeTicks;
        public final Set<Integer> chargeHit = new HashSet<>();
        public int slamTicks;
        public long alientoReady;
        public long shieldHealReady;
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    public static State state(ServerPlayer p) {
        return STATES.computeIfAbsent(p.getUUID(), k -> new State());
    }

    public static void forget(ServerPlayer p) {
        STATES.remove(p.getUUID());
    }

    public static void use(ServerPlayer p, int slot) {
        if (slot < 0 || slot > 3 || !p.isAlive() || p.isSpectator()) return;
        GuildClass c = GuildGate.classOf(p);
        if (c == null) {
            p.displayClientMessage(Component.translatable("msg.clases_gremio.sin_clase").withStyle(ChatFormatting.RED), true);
            return;
        }
        GuildAbility a = GuildAbility.get(c, slot);
        if (a == null) return;
        if (!p.isCreative() && GuildGate.stageOf(p) < slot) {
            p.displayClientMessage(Component.translatable("msg.clases_gremio.habilidad_rango",
                    GuildGate.RANGO_MINIMO[slot].letter()).withStyle(ChatFormatting.RED), true);
            return;
        }
        State s = state(p);
        long now = p.level().getGameTime();
        if (now < s.cooldownEnd[slot]) {
            PacketDistributor.sendToPlayer(p, new CooldownPayload(slot, (int) (s.cooldownEnd[slot] - now), a.cooldown()));
            return;
        }
        boolean used = switch (c) {
            case GUERRERO -> GuerreroHabilidades.use(p, a, s);
            default -> false;
        };
        if (used) {
            s.cooldownEnd[slot] = now + a.cooldown();
            PacketDistributor.sendToPlayer(p, new CooldownPayload(slot, a.cooldown(), a.cooldown()));
        }
    }

    private AbilityManager() {
    }
}
