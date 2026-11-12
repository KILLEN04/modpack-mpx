package com.mpxcosos.clasesgremio.ability;

import com.mpxcosos.clasesgremio.network.FxPayload;
import com.mpxcosos.clasesgremio.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/** Atajos del servidor para mandar efectos de fuego a los jugadores cercanos. */
public final class Fx {
    private static final double RANGE = 96;

    public static void send(ServerLevel level, int type, Entity entity, Vec3 pos, float a, float b, float c) {
        PacketDistributor.sendToPlayersNear(level, null, pos.x, pos.y, pos.z, RANGE,
                new FxPayload(type, entity == null ? -1 : entity.getId(), pos.x, pos.y, pos.z, a, b, c));
    }

    public static void trail(Entity e, int ticks, float width) {
        send((ServerLevel) e.level(), FxPayload.TRAIL, e, e.position(), ticks, width, 0);
    }

    public static void ring(ServerLevel l, Vec3 p, float radius, int ticks, int delay) {
        send(l, FxPayload.RING, null, p, radius, ticks, delay);
    }

    public static void pillar(ServerLevel l, Vec3 p, float height, int ticks, int delay) {
        send(l, FxPayload.PILLAR, null, p, height, ticks, delay);
    }

    public static void aura(Entity e, int ticks) {
        send((ServerLevel) e.level(), FxPayload.AURA, e, e.position(), ticks, 0, 0);
    }

    public static void xslash(ServerLevel l, Vec3 p, float yaw, float size) {
        send(l, FxPayload.XSLASH, null, p, yaw, size, 0);
    }

    public static void shake(ServerLevel l, Vec3 p, float intensity, float radius) {
        send(l, FxPayload.SHAKE, null, p, intensity, radius, 0);
    }

    public static void burst(ServerLevel l, Vec3 p, float size) {
        send(l, FxPayload.BURST, null, p, size, 0, 0);
    }

    public static void slamArc(Entity e, float radius) {
        send((ServerLevel) e.level(), FxPayload.SLAM_ARC, e, e.position(), e.getYRot(), radius, 0);
    }

    public static void embers(ServerLevel l, Vec3 p, int count, double spread, double speed) {
        l.sendParticles(ModParticles.BRASA.get(), p.x, p.y, p.z, count, spread, spread * 0.6, spread, speed);
    }

    private Fx() {
    }
}
