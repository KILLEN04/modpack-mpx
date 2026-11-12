package com.mpxcosos.clasesgremio.registry;

import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.item.ArcoGremioItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ClasesGremio.MODID);

    public static final DeferredItem<DeferredSpawnEggItem> RECEPCIONISTA_SPAWN_EGG = ITEMS.register(
            "recepcionista_spawn_egg",
            () -> new DeferredSpawnEggItem(ModEntities.RECEPCIONISTA, 0x1E2747, 0xE2B548, new Item.Properties()));

    // Arcos del Arquero (etapas F-E, D-C, B-A, S)
    public static final DeferredItem<ArcoGremioItem> ARCO_CAZADOR = ITEMS.register("arco_cazador",
            () -> new ArcoGremioItem(0, 26, new Item.Properties().durability(384)));
    public static final DeferredItem<ArcoGremioItem> ARCO_VIENTO = ITEMS.register("arco_viento",
            () -> new ArcoGremioItem(1, 34, new Item.Properties().durability(600).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<ArcoGremioItem> ARCO_ESCARCHA = ITEMS.register("arco_escarcha",
            () -> new ArcoGremioItem(2, 40, new Item.Properties().durability(900).rarity(Rarity.RARE)));
    public static final DeferredItem<ArcoGremioItem> ARCO_TORMENTA = ITEMS.register("arco_tormenta",
            () -> new ArcoGremioItem(3, 46, new Item.Properties().durability(1500).rarity(Rarity.EPIC).fireResistant()));

    private ModItems() {
    }
}
