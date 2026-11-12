package com.mpxcosos.clasesgremio.registry;

import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.item.GremioTier;
import com.mpxcosos.clasesgremio.item.GuerreroArmaItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.List;

/** Armas de clase por etapa. Modelos de Hazen N Stuff (con permiso), stats propios. */
public final class ModWeapons {
    // daño total = base del jugador (1) + valor; velocidad = 4 + valor
    public static final DeferredItem<GuerreroArmaItem> GUERRERO_1 = reg(1, 0, false, 500, Items.IRON_INGOT, GremioTier.iron(), 5f, -2.4f, Rarity.COMMON);
    public static final DeferredItem<GuerreroArmaItem> GUERRERO_2 = reg(2, 1, true, 900, Items.IRON_INGOT, GremioTier.iron(), 6.5f, -2.8f, Rarity.UNCOMMON);
    public static final DeferredItem<GuerreroArmaItem> GUERRERO_3 = reg(3, 2, false, 1600, Items.DIAMOND, GremioTier.diamond(), 8f, -3.0f, Rarity.RARE);
    public static final DeferredItem<GuerreroArmaItem> GUERRERO_4 = reg(4, 3, true, 2600, Items.NETHERITE_INGOT, GremioTier.netherite(), 10f, -2.8f, Rarity.EPIC);

    public static final List<DeferredItem<GuerreroArmaItem>> GUERRERO = List.of(GUERRERO_1, GUERRERO_2, GUERRERO_3, GUERRERO_4);

    private static DeferredItem<GuerreroArmaItem> reg(int n, int stage, boolean glow, int uses, Item repair,
                                                      net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> incorrect,
                                                      float damage, float speed, Rarity rarity) {
        return ModItems.ITEMS.register("arma_guerrero_" + n, () -> {
            GremioTier tier = new GremioTier(uses, 6f + stage, 12 + stage * 2, incorrect, repair);
            Item.Properties props = new Item.Properties().rarity(rarity)
                    .attributes(SwordItem.createAttributes(tier, damage, speed));
            if (stage == 3) props = props.fireResistant();
            return new GuerreroArmaItem(stage, glow, tier, props);
        });
    }

    /** Arma de una clase en una etapa, o null. */
    public static Item get(GuildClass c, int stage) {
        if (c == GuildClass.GUERRERO && stage >= 0 && stage < 4) return GUERRERO.get(stage).get();
        return null;
    }

    public static void init() {
    }

    private ModWeapons() {
    }
}
