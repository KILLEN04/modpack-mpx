package com.mpxcosos.clasesgremio.entity;

import com.mpxcosos.clasesgremio.network.ModNetwork;
import com.mpxcosos.clasesgremio.network.OpenRecepcionistaPayload;
import com.mpxcosos.clasesgremio.registry.ModAttachments;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** La recepcionista del gremio: inmortal, no se mueve y abre el menú de clases. */
public class RecepcionistaEntity extends PathfinderMob implements GeoEntity {
    public static final String CONTROLLER = "main";

    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.idle");
    private static final RawAnimation GREET = RawAnimation.begin().thenPlay("animation.greet");
    private static final RawAnimation TALK = RawAnimation.begin().thenPlayXTimes("animation.talk", 2);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public RecepcionistaEntity(EntityType<? extends RecepcionistaEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.setInvulnerable(true);
        this.setCustomName(Component.translatable("entity.clases_gremio.recepcionista.nombre"));
        this.setCustomNameVisible(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (!this.level().isClientSide && player instanceof ServerPlayer sp) {
            ModNetwork.sync(sp);
            PacketDistributor.sendToPlayer(sp, new OpenRecepcionistaPayload(this.getId()));
            boolean hasClass = sp.getData(ModAttachments.GUILD_DATA).hasClass();
            this.triggerAnim(CONTROLLER, hasClass ? "talk" : "greet");
            this.getLookControl().setLookAt(player, 30.0f, 30.0f);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** Solo /kill (o daño que ignora invulnerabilidad) puede quitarla. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(net.minecraft.world.entity.Entity entity) {
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    // ---- GeckoLib ----

    public void playTalk() {
        this.triggerAnim(CONTROLLER, "talk");
    }

    public void playGreet() {
        this.triggerAnim(CONTROLLER, "greet");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, CONTROLLER, 5, state -> state.setAndContinue(IDLE))
                .triggerableAnim("greet", GREET)
                .triggerableAnim("talk", TALK));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
