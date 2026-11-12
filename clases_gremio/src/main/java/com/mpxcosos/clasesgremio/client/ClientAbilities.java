package com.mpxcosos.clasesgremio.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mpxcosos.clasesgremio.client.fx.CameraShake;
import com.mpxcosos.clasesgremio.client.fx.FireDraw;
import com.mpxcosos.clasesgremio.client.fx.FireFx;
import com.mpxcosos.clasesgremio.item.GuerreroArmaItem;
import com.mpxcosos.clasesgremio.network.CooldownPayload;
import com.mpxcosos.clasesgremio.network.UseAbilityPayload;
import com.mpxcosos.clasesgremio.registry.ModSounds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

/** Teclas de habilidades, recargas en el cliente, estela de cada espadazo y dibujo de efectos. */
public final class ClientAbilities {
    public static final String CATEGORY = "key.categories.clases_gremio";
    public static final KeyMapping[] HABILIDAD = {
            new KeyMapping("key.clases_gremio.habilidad_1", InputConstants.Type.KEYSYM, InputConstants.KEY_R, CATEGORY),
            new KeyMapping("key.clases_gremio.habilidad_2", InputConstants.Type.KEYSYM, InputConstants.KEY_G, CATEGORY),
            new KeyMapping("key.clases_gremio.habilidad_3", InputConstants.Type.KEYSYM, InputConstants.KEY_H, CATEGORY),
            new KeyMapping("key.clases_gremio.habilidad_4", InputConstants.Type.KEYSYM, InputConstants.KEY_Y, CATEGORY)};
    public static final KeyMapping LIBRO =
            new KeyMapping("key.clases_gremio.libro", InputConstants.Type.KEYSYM, InputConstants.KEY_K, CATEGORY);

    private static final long[] COOLDOWN_END = new long[4];
    private static final int[] COOLDOWN_TOTAL = new int[4];
    private static final Map<Integer, Integer> LAST_SWING = new HashMap<>();
    private static final Map<Integer, Integer> SWING_SIDE = new HashMap<>();

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        for (KeyMapping k : HABILIDAD) event.register(k);
        event.register(LIBRO);
    }

    public static void setCooldown(CooldownPayload p) {
        var level = Minecraft.getInstance().level;
        if (level == null || p.slot() < 0 || p.slot() > 3) return;
        COOLDOWN_END[p.slot()] = level.getGameTime() + p.remaining();
        COOLDOWN_TOTAL[p.slot()] = Math.max(1, p.total());
    }

    /** 0 = lista, 1 = recién usada. */
    public static float cooldownFraction(int slot, float partial) {
        var level = Minecraft.getInstance().level;
        if (level == null) return 0;
        float left = COOLDOWN_END[slot] - (level.getGameTime() + partial);
        return left <= 0 ? 0 : Math.min(1, left / COOLDOWN_TOTAL[slot]);
    }

    public static int cooldownSeconds(int slot) {
        var level = Minecraft.getInstance().level;
        if (level == null) return 0;
        long left = COOLDOWN_END[slot] - level.getGameTime();
        return left <= 0 ? 0 : (int) Math.ceil(left / 20.0);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        FireFx.tick();
        CameraShake.tick();
        if (mc.level == null || mc.player == null) return;
        if (mc.screen == null) {
            for (int i = 0; i < HABILIDAD.length; i++) {
                while (HABILIDAD[i].consumeClick()) PacketDistributor.sendToServer(new UseAbilityPayload(i));
            }
            while (LIBRO.consumeClick()) mc.setScreen(new LibroClaseScreen());
        }
        // estela de fuego en cada espadazo de un arma del Guerrero (de cualquier jugador cercano)
        for (AbstractClientPlayer p : mc.level.players()) {
            int id = p.getId();
            int st = p.swinging ? p.swingTime : -1;
            int prev = LAST_SWING.getOrDefault(id, -1);
            if (p.swinging && (prev == -1 || st < prev) && p.getMainHandItem().getItem() instanceof GuerreroArmaItem arma) {
                int side = -SWING_SIDE.getOrDefault(id, 1);
                SWING_SIDE.put(id, side);
                spawnSwing(p, arma.stage(), side);
            }
            LAST_SWING.put(id, st);
        }
    }

    private static void spawnSwing(AbstractClientPlayer p, int tier, int side) {
        Vec3 center = p.getEyePosition().add(0, -0.35, 0);
        float radius = 1.5f + tier * 0.25f;
        float width = 0.4f + tier * 0.16f;
        float roll = side * (22f + p.getRandom().nextFloat() * 18f);
        FireFx.add(new FireFx.Arc(center, p.getYRot(), p.getXRot(), radius, width, roll, false, tier, side));
        p.level().playLocalSound(p.getX(), p.getY(), p.getZ(), ModSounds.ESPADAZO.get(), SoundSource.PLAYERS,
                0.35f + tier * 0.1f, 0.9f + p.getRandom().nextFloat() * 0.25f, false);
    }

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        var camera = event.getCamera();
        PoseStack ps = new PoseStack();
        var buffers = mc.renderBuffers().bufferSource();
        var left = camera.getLeftVector();
        var up = camera.getUpVector();
        FireDraw.Ctx ctx = new FireDraw.Ctx(ps.last(), buffers, camera.getPosition(),
                new Vec3(-left.x(), -left.y(), -left.z()), new Vec3(up.x(), up.y(), up.z()),
                event.getPartialTick().getGameTimeDeltaPartialTick(false), mc.level.getGameTime());
        FireFx.render(ctx);
        buffers.endBatch();
    }

    private ClientAbilities() {
    }
}
