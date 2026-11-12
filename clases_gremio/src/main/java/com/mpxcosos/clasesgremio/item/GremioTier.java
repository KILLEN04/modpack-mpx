package com.mpxcosos.clasesgremio.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/** Material propio de las armas del gremio. */
public record GremioTier(int uses, float speed, int enchant, TagKey<Block> incorrect, ItemLike repair) implements Tier {
    @Override public int getUses() { return uses; }
    @Override public float getSpeed() { return speed; }
    @Override public float getAttackDamageBonus() { return 0f; }
    @Override public TagKey<Block> getIncorrectBlocksForDrops() { return incorrect; }
    @Override public int getEnchantmentValue() { return enchant; }
    @Override public Ingredient getRepairIngredient() { return Ingredient.of(repair); }

    public static TagKey<Block> iron() { return BlockTags.INCORRECT_FOR_IRON_TOOL; }
    public static TagKey<Block> diamond() { return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; }
    public static TagKey<Block> netherite() { return BlockTags.INCORRECT_FOR_NETHERITE_TOOL; }
}
