package com.mpxcosos.clasesgremio.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

/** Partícula propia del gremio; el comportamiento cambia según el tipo. */
public class GremioParticle extends TextureSheetParticle {
    public enum Tipo { VIENTO, ESCARCHA, CHISPA, ESTRELLA, RAYO, BRASA }

    private final SpriteSet sprites;
    private final Tipo tipo;
    private final float baseSize;

    public GremioParticle(ClientLevel level, double x, double y, double z,
                          double dx, double dy, double dz, SpriteSet sprites, Tipo tipo) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.tipo = tipo;
        this.xd = dx;
        this.yd = dy;
        this.zd = dz;
        this.hasPhysics = false;
        switch (tipo) {
            case VIENTO -> { lifetime = 14 + random.nextInt(6); quadSize = 0.2f; gravity = -0.01f; friction = 0.9f; }
            case ESCARCHA -> { lifetime = 22 + random.nextInt(10); quadSize = 0.11f; gravity = 0.04f; friction = 0.96f; }
            case CHISPA -> { lifetime = 6 + random.nextInt(4); quadSize = 0.13f; gravity = 0f; friction = 0.8f; }
            case ESTRELLA -> { lifetime = 14 + random.nextInt(8); quadSize = 0.1f; gravity = 0f; friction = 0.85f; }
            case RAYO -> { lifetime = 7 + random.nextInt(3); quadSize = 0.3f; gravity = 0f; friction = 0f;
                           xd = 0; yd = 0; zd = 0; }
            case BRASA -> { lifetime = 16 + random.nextInt(14); quadSize = 0.07f + random.nextFloat() * 0.05f;
                            gravity = -0.03f; friction = 0.94f; yd += 0.02; }
        }
        this.baseSize = quadSize;
        if (tipo == Tipo.ESCARCHA) pickSprite(sprites);
        else setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (removed) return;
        if (tipo != Tipo.ESCARCHA) setSpriteFromAge(sprites);
        float t = (float) age / lifetime;
        switch (tipo) {
            case VIENTO, RAYO, CHISPA, BRASA -> alpha = 1f - t * t;
            case ESCARCHA -> alpha = t > 0.7f ? (1f - t) / 0.3f : 1f;
            case ESTRELLA -> quadSize = baseSize * (0.6f + 0.4f * Mth.sin(age * 0.9f)) * (1f - t * 0.5f);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected int getLightColor(float partialTick) {
        return tipo == Tipo.VIENTO || tipo == Tipo.ESCARCHA ? super.getLightColor(partialTick) : 0xF000F0;
    }
}
