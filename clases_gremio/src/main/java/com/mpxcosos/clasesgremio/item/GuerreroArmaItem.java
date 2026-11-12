package com.mpxcosos.clasesgremio.item;

import com.mpxcosos.clasesgremio.ability.Fx;
import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.data.GuildGate;
import com.mpxcosos.clasesgremio.registry.ModDamage;
import com.mpxcosos.clasesgremio.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * Arma del Guerrero. Etapa 0 Mage Bane (sangrado), 1 Firebrand (quemadura),
 * 2 Ancient Warrior's Axe (rompearmaduras), 3 Draconic Splitter (tajo dracónico).
 * Modelos de Hazen N Stuff, usados con permiso. Cada espadazo deja estela de fuego (cliente).
 */
public class GuerreroArmaItem extends SwordItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final int stage;
    private final boolean glow;

    public GuerreroArmaItem(int stage, boolean glow, GremioTier tier, Properties properties) {
        super(tier, properties);
        this.stage = stage;
        this.glow = glow;
    }

    public int stage() {
        return stage;
    }

    public boolean hasGlow() {
        return glow;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker.level() instanceof ServerLevel level) {
            var r = level.random;
            Vec3 hit = target.position().add(0, target.getBbHeight() * 0.6, 0);
            switch (stage) {
                case 0 -> {
                    if (r.nextFloat() < 0.35f) target.addEffect(new MobEffectInstance(ModEffects.SANGRADO, 60, 0), attacker);
                }
                case 1 -> {
                    target.addEffect(new MobEffectInstance(ModEffects.QUEMADURA, 60, 0), attacker);
                    if (r.nextFloat() < 0.2f) target.addEffect(new MobEffectInstance(ModEffects.SANGRADO, 60, 0), attacker);
                    Fx.embers(level, hit, 10, 0.3, 0.05);
                }
                case 2 -> {
                    target.addEffect(new MobEffectInstance(ModEffects.ARMADURA_ROTA, 100, 0), attacker);
                    if (r.nextFloat() < 0.4f) target.addEffect(new MobEffectInstance(ModEffects.SANGRADO, 60, 0), attacker);
                    Fx.embers(level, hit, 14, 0.35, 0.06);
                }
                default -> {
                    target.addEffect(new MobEffectInstance(ModEffects.QUEMADURA, 80, 1), attacker);
                    float splash = (float) attacker.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.4f;
                    for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.5))) {
                        if (e == target || e == attacker || !e.isAlive()) continue;
                        e.hurt(ModDamage.source(level, ModDamage.GUERRERO, attacker), splash);
                        e.addEffect(new MobEffectInstance(ModEffects.QUEMADURA, 60, 0), attacker);
                    }
                    Fx.xslash(level, hit, attacker.getYRot(), 1.3f);
                    Fx.embers(level, hit, 24, 0.5, 0.08);
                }
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.clases_gremio.arma_clase", GuildClass.GUERRERO.displayName())
                .withColor(GuildClass.GUERRERO.color()));
        var rank = GuildGate.RANGO_MINIMO[stage];
        tooltip.add(Component.translatable("tooltip.clases_gremio.armadura_rango",
                Component.literal(rank.letter()).withColor(rank.color())).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.clases_gremio.arma_guerrero_" + (stage + 1))
                .withStyle(ChatFormatting.GOLD));
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null)
                    renderer = new com.mpxcosos.clasesgremio.client.GuerreroArmaRenderer(GuerreroArmaItem.this);
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
