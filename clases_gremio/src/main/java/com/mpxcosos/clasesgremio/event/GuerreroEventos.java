package com.mpxcosos.clasesgremio.event;

import com.mpxcosos.clasesgremio.ability.AbilityManager;
import com.mpxcosos.clasesgremio.ability.Fx;
import com.mpxcosos.clasesgremio.ability.GuerreroHabilidades;
import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.data.GuildGate;
import com.mpxcosos.clasesgremio.item.GuerreroArmaItem;
import com.mpxcosos.clasesgremio.registry.ModEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Eventos del Guerrero: habilidades en curso, pasivas, Baluarte, Furia y bloqueo de armas. */
public final class GuerreroEventos {
    public static void register() {
        NeoForge.EVENT_BUS.addListener(GuerreroEventos::onPlayerTick);
        NeoForge.EVENT_BUS.addListener(GuerreroEventos::onIncoming);
        NeoForge.EVENT_BUS.addListener(GuerreroEventos::onDamagePost);
        NeoForge.EVENT_BUS.addListener(GuerreroEventos::onShield);
        NeoForge.EVENT_BUS.addListener(GuerreroEventos::onDeath);
        NeoForge.EVENT_BUS.addListener(GuerreroEventos::onAttack);
        NeoForge.EVENT_BUS.addListener(GuerreroEventos::onLogout);
    }

    private static void onPlayerTick(PlayerTickEvent.Post e) {
        if (e.getEntity() instanceof ServerPlayer p) GuerreroHabilidades.tick(p);
    }

    /** Baluarte: 35 % menos de daño. */
    private static void onIncoming(LivingIncomingDamageEvent e) {
        if (e.getEntity().hasEffect(ModEffects.BALUARTE)) e.setAmount(e.getAmount() * 0.65f);
    }

    /** Furia: robo de vida y tajo en X en cada golpe cuerpo a cuerpo. */
    private static void onDamagePost(LivingDamageEvent.Post e) {
        if (!(e.getSource().getDirectEntity() instanceof ServerPlayer p) || !p.hasEffect(ModEffects.FURIA)) return;
        LivingEntity target = e.getEntity();
        p.heal(e.getNewDamage() * 0.25f);
        Fx.xslash((ServerLevel) p.level(), target.position().add(0, target.getBbHeight() * 0.55, 0), p.getYRot(), 1.2f);
    }

    private static void onShield(LivingShieldBlockEvent e) {
        if (e.getBlocked() && e.getEntity() instanceof ServerPlayer p) GuerreroHabilidades.onShieldBlock(p);
    }

    private static void onDeath(LivingDeathEvent e) {
        if (e.getEntity() instanceof ServerPlayer p && GuerreroHabilidades.onDeath(p)) e.setCanceled(true);
    }

    /** Las armas del Guerrero solo las usa un Guerrero con el rango suficiente. */
    private static void onAttack(AttackEntityEvent e) {
        var p = e.getEntity();
        if (p.getMainHandItem().getItem() instanceof GuerreroArmaItem arma) {
            Component reason = GuildGate.denyReason(p, GuildClass.GUERRERO, arma.stage(), "arma");
            if (reason != null) {
                e.setCanceled(true);
                if (!p.level().isClientSide)
                    p.displayClientMessage(reason.copy().withStyle(ChatFormatting.RED), true);
            }
        }
    }

    private static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {
        if (e.getEntity() instanceof ServerPlayer p) AbilityManager.forget(p);
    }

    private GuerreroEventos() {
    }
}
