package com.mpxcosos.clasesgremio.item;

import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.data.GuildRank;
import com.mpxcosos.clasesgremio.data.PlayerGuildData;
import com.mpxcosos.clasesgremio.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

/**
 * Pieza de armadura del gremio. Solo la puede llevar su clase y con el rango suficiente
 * (etapa 0 = F-E, 1 = D-C, 2 = B-A, 3 = S). En creativo no hay restricciones.
 * Modelo: assets/clases_gremio/geo/item/armor/<setId>.geo.json
 */
public class GuildArmorItem extends ArmorItem implements GeoItem {
    private static final GuildRank[] RANGO_MINIMO = {GuildRank.F, GuildRank.D, GuildRank.B, GuildRank.S};

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String setId;
    private final GuildClass guildClass;
    private final int stage;

    public GuildArmorItem(String setId, GuildClass guildClass, int stage, Holder<ArmorMaterial> material,
                          Type type, Properties properties) {
        super(material, type, properties);
        this.setId = setId;
        this.guildClass = guildClass;
        this.stage = stage;
    }

    public String setId() {
        return setId;
    }

    public GuildClass guildClass() {
        return guildClass;
    }

    public int stage() {
        return stage;
    }

    public GuildRank requiredRank() {
        return RANGO_MINIMO[stage];
    }

    /** null si puede usarla; si no, el mensaje de por qué no. */
    @Nullable
    public Component denyReason(Player player) {
        if (player.isCreative()) return null;
        PlayerGuildData data = player.getData(ModAttachments.GUILD_DATA);
        if (!guildClass.id().equals(data.classId()))
            return Component.translatable("msg.clases_gremio.armadura_clase", guildClass.displayName());
        if (GuildRank.byIndex(data.rank()).armorStage() < stage)
            return Component.translatable("msg.clases_gremio.armadura_rango", requiredRank().letter());
        return null;
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot slot, LivingEntity entity) {
        if (entity instanceof Player player && denyReason(player) != null) return false;
        return super.canEquip(stack, slot, entity);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        Component reason = denyReason(player);
        if (reason != null) {
            if (!level.isClientSide) player.displayClientMessage(reason.copy().withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.clases_gremio.armadura_clase", guildClass.displayName())
                .withColor(guildClass.color()));
        tooltip.add(Component.translatable("tooltip.clases_gremio.armadura_rango",
                Component.literal(requiredRank().letter()).withColor(requiredRank().color()))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private HumanoidModel<?> renderer;

            @Override
            public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(@Nullable T livingEntity, ItemStack itemStack,
                                                                                 @Nullable EquipmentSlot equipmentSlot,
                                                                                 @Nullable HumanoidModel<T> original) {
                if (renderer == null)
                    renderer = new com.mpxcosos.clasesgremio.client.GuildArmorRenderer(setId);
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
