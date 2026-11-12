package com.mpxcosos.clasesgremio.client;

import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.registry.ModEntities;
import com.mpxcosos.clasesgremio.registry.ModParticles;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import com.mpxcosos.clasesgremio.client.fx.CameraShake;

/** Parte de cliente del mod (no se carga en servidores dedicados). */
@Mod(value = ClasesGremio.MODID, dist = Dist.CLIENT)
public class ClasesGremioClient {
    public ClasesGremioClient(IEventBus modBus) {
        modBus.addListener(ClasesGremioClient::onRegisterRenderers);
        modBus.addListener(ClasesGremioClient::onRegisterParticles);
        modBus.addListener(ClientAbilities::registerKeys);
        modBus.addListener(ClasesGremioClient::onGuiLayers);
        NeoForge.EVENT_BUS.addListener(ClientAbilities::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientAbilities::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(CameraShake::onCamera);
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.RECEPCIONISTA.get(), RecepcionistaRenderer::new);
    }

    private static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        particle(event, ModParticles.VIENTO.get(), GremioParticle.Tipo.VIENTO);
        particle(event, ModParticles.ESCARCHA.get(), GremioParticle.Tipo.ESCARCHA);
        particle(event, ModParticles.CHISPA.get(), GremioParticle.Tipo.CHISPA);
        particle(event, ModParticles.ESTRELLA.get(), GremioParticle.Tipo.ESTRELLA);
        particle(event, ModParticles.RAYO.get(), GremioParticle.Tipo.RAYO);
        particle(event, ModParticles.BRASA.get(), GremioParticle.Tipo.BRASA);
    }

    private static void particle(RegisterParticleProvidersEvent event, SimpleParticleType type, GremioParticle.Tipo tipo) {
        event.registerSpriteSet(type, sprites -> (t, level, x, y, z, dx, dy, dz) ->
                new GremioParticle(level, x, y, z, dx, dy, dz, sprites, tipo));
    }

    private static void onGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, HabilidadesHud.ID, HabilidadesHud::render);
    }
}
