package com.mpxcosos.clasesgremio;

import com.mojang.logging.LogUtils;
import com.mpxcosos.clasesgremio.command.GremioCommand;
import com.mpxcosos.clasesgremio.entity.RecepcionistaEntity;
import com.mpxcosos.clasesgremio.event.ArcoEventos;
import com.mpxcosos.clasesgremio.event.GuerreroEventos;
import com.mpxcosos.clasesgremio.network.ModNetwork;
import com.mpxcosos.clasesgremio.registry.ModArmor;
import com.mpxcosos.clasesgremio.registry.ModAttachments;
import com.mpxcosos.clasesgremio.registry.ModEntities;
import com.mpxcosos.clasesgremio.registry.ModEffects;
import com.mpxcosos.clasesgremio.registry.ModItems;
import com.mpxcosos.clasesgremio.registry.ModParticles;
import com.mpxcosos.clasesgremio.registry.ModSounds;
import com.mpxcosos.clasesgremio.registry.ModWeapons;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.slf4j.Logger;

@Mod(ClasesGremio.MODID)
public class ClasesGremio {
    public static final String MODID = "clases_gremio";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ClasesGremio(IEventBus modBus) {
        ModEntities.ENTITIES.register(modBus);
        ModArmor.init();
        ModWeapons.init();
        ModArmor.MATERIALS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModAttachments.ATTACHMENT_TYPES.register(modBus);
        ModParticles.PARTICLES.register(modBus);
        ModSounds.SOUNDS.register(modBus);
        ModEffects.EFFECTS.register(modBus);

        modBus.addListener(ClasesGremio::onAttributes);
        modBus.addListener(ClasesGremio::onCreativeTabs);
        modBus.addListener(ModNetwork::register);

        ArcoEventos.register();
        GuerreroEventos.register();
        NeoForge.EVENT_BUS.addListener(ClasesGremio::onCommands);
        NeoForge.EVENT_BUS.addListener(ClasesGremio::onLogin);
        NeoForge.EVENT_BUS.addListener(ClasesGremio::onRespawn);
        NeoForge.EVENT_BUS.addListener(ClasesGremio::onChangeDimension);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private static void onAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.RECEPCIONISTA.get(), RecepcionistaEntity.createAttributes().build());
    }

    private static void onCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.RECEPCIONISTA_SPAWN_EGG.get());
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.ARCO_CAZADOR.get());
            event.accept(ModItems.ARCO_VIENTO.get());
            event.accept(ModItems.ARCO_ESCARCHA.get());
            event.accept(ModItems.ARCO_TORMENTA.get());
            ModWeapons.GUERRERO.forEach(w -> event.accept(w.get()));
            for (ModArmor.ArmorSet set : ModArmor.SETS) set.pieces().forEach(p -> event.accept(p.get()));
        }
    }

    private static void onCommands(RegisterCommandsEvent event) {
        GremioCommand.register(event.getDispatcher());
    }

    private static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) ModNetwork.sync(sp);
    }

    private static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) ModNetwork.sync(sp);
    }

    private static void onChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) ModNetwork.sync(sp);
    }
}
