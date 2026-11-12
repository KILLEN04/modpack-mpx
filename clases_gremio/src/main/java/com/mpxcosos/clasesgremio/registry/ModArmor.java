package com.mpxcosos.clasesgremio.registry;

import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.item.GuildArmorItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Armaduras de clase. Cada clase tiene 4 etapas (F-E, D-C, B-A, S); Lyra mejora la etapa al subir de rango.
 * Stats propios del gremio (no los de los mods originales).
 * Modelos del Guerrero: Fantasy Armor (Kenddie y mon1tor), usados con permiso.
 */
public final class ModArmor {
    public static final DeferredRegister<ArmorMaterial> MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, ClasesGremio.MODID);

    /** Todos los sets registrados, en orden (para la pestaña creativa). */
    public static final List<ArmorSet> SETS = new ArrayList<>();

    // ---------------- Guerrero ----------------
    //                                       casco peto grebas botas  dureza/pieza  empuje/pieza  encant.  reparación
    public static final ArmorSet GUERRERO_1 = set("guerrero_1", GuildClass.GUERRERO, 0, 2, 3, 2, 1, 0f, 0f, 15, Items.LEATHER, 12);
    public static final ArmorSet GUERRERO_2 = set("guerrero_2", GuildClass.GUERRERO, 1, 2, 6, 6, 2, 0.25f, 0f, 12, Items.IRON_INGOT, 20);
    public static final ArmorSet GUERRERO_3 = set("guerrero_3", GuildClass.GUERRERO, 2, 3, 8, 7, 3, 0.625f, 0.025f, 12, Items.DIAMOND, 33);
    public static final ArmorSet GUERRERO_4 = set("guerrero_4", GuildClass.GUERRERO, 3, 3, 8, 7, 3, 0.875f, 0.075f, 15, Items.NETHERITE_INGOT, 40);

    /** Un set de 4 piezas de una clase y etapa. */
    public record ArmorSet(String id, GuildClass guildClass, int stage,
                           DeferredItem<GuildArmorItem> helmet, DeferredItem<GuildArmorItem> chestplate,
                           DeferredItem<GuildArmorItem> leggings, DeferredItem<GuildArmorItem> boots) {
        public List<DeferredItem<GuildArmorItem>> pieces() {
            return List.of(helmet, chestplate, leggings, boots);
        }
    }

    private static ArmorSet set(String id, GuildClass guildClass, int stage,
                                int helmet, int chest, int legs, int boots,
                                float toughness, float knockback, int enchant, ItemLike repair, int durabilityMult) {
        DeferredHolder<ArmorMaterial, ArmorMaterial> material = MATERIALS.register(id, () -> {
            Map<ArmorItem.Type, Integer> def = new EnumMap<>(ArmorItem.Type.class);
            def.put(ArmorItem.Type.HELMET, helmet);
            def.put(ArmorItem.Type.CHESTPLATE, chest);
            def.put(ArmorItem.Type.LEGGINGS, legs);
            def.put(ArmorItem.Type.BOOTS, boots);
            def.put(ArmorItem.Type.BODY, chest);
            return new ArmorMaterial(def, enchant, ModSounds.ARMADURA_EQUIPAR, () -> Ingredient.of(repair),
                    List.of(new ArmorMaterial.Layer(ClasesGremio.id(id))), toughness, knockback);
        });
        Rarity rarity = switch (stage) {
            case 0 -> Rarity.COMMON;
            case 1 -> Rarity.UNCOMMON;
            case 2 -> Rarity.RARE;
            default -> Rarity.EPIC;
        };
        ArmorSet s = new ArmorSet(id, guildClass, stage,
                piece(id, "casco", guildClass, stage, material, ArmorItem.Type.HELMET, durabilityMult, rarity),
                piece(id, "peto", guildClass, stage, material, ArmorItem.Type.CHESTPLATE, durabilityMult, rarity),
                piece(id, "grebas", guildClass, stage, material, ArmorItem.Type.LEGGINGS, durabilityMult, rarity),
                piece(id, "botas", guildClass, stage, material, ArmorItem.Type.BOOTS, durabilityMult, rarity));
        SETS.add(s);
        return s;
    }

    private static DeferredItem<GuildArmorItem> piece(String id, String part, GuildClass guildClass, int stage,
                                                      DeferredHolder<ArmorMaterial, ArmorMaterial> material,
                                                      ArmorItem.Type type, int durabilityMult, Rarity rarity) {
        return ModItems.ITEMS.register(id + "_" + part, () -> {
            Item.Properties props = new Item.Properties().durability(type.getDurability(durabilityMult)).rarity(rarity);
            if (stage == 3) props = props.fireResistant();
            return new GuildArmorItem(id, guildClass, stage, material, type, props);
        });
    }

    /** Set de una clase en una etapa, o null si aún no existe. */
    public static ArmorSet get(GuildClass guildClass, int stage) {
        for (ArmorSet s : SETS) {
            if (s.guildClass() == guildClass && s.stage() == stage) return s;
        }
        return null;
    }

    /** Fuerza la carga de la clase (registra los sets) antes de que se cierre el registro. */
    public static void init() {
    }

    private ModArmor() {
    }
}
