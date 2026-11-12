package com.mpxcosos.clasesgremio.item;

import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * Arco yumi del Arquero. Cada etapa (0 = F-E ... 3 = S) es más grande y suma un elemento:
 * 1 viento (flechas más rápidas y empuje), 2 + hielo (lentitud), 3 + tormenta (posible rayo).
 * Los poderes se aplican en {@link com.mpxcosos.clasesgremio.event.ArcoEventos}.
 */
public class ArcoGremioItem extends BowItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final int stage;
    private final int lengthPx;

    public ArcoGremioItem(int stage, int lengthPx, Properties properties) {
        super(properties);
        this.stage = stage;
        this.lengthPx = lengthPx;
    }

    public int stage() {
        return stage;
    }

    /** Largo aproximado del modelo en píxeles (para escalarlo en el inventario). */
    public int lengthPx() {
        return lengthPx;
    }

    /** Velocidad extra de la flecha por el viento. */
    public float speedMultiplier() {
        return 1.0f + 0.12f * stage;
    }

    public static boolean isArcher(Player player) {
        return GuildClass.ARQUERO.id().equals(player.getData(ModAttachments.GUILD_DATA).classId());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.isCreative() && !isArcher(player)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("msg.clases_gremio.solo_arquero")
                        .withStyle(ChatFormatting.RED), true);
            }
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.clases_gremio.solo_arquero").withStyle(ChatFormatting.DARK_GREEN));
        if (stage >= 1) tooltip.add(Component.translatable("tooltip.clases_gremio.viento").withStyle(ChatFormatting.WHITE));
        if (stage >= 2) tooltip.add(Component.translatable("tooltip.clases_gremio.hielo").withStyle(ChatFormatting.AQUA));
        if (stage >= 3) tooltip.add(Component.translatable("tooltip.clases_gremio.tormenta").withStyle(ChatFormatting.BLUE));
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (renderer == null)
                    renderer = new com.mpxcosos.clasesgremio.client.ArcoGremioRenderer(ArcoGremioItem.this);
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
