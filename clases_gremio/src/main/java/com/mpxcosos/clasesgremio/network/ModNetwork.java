package com.mpxcosos.clasesgremio.network;

import com.mpxcosos.clasesgremio.client.ClientPayloadHandler;
import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.data.GuildRank;
import com.mpxcosos.clasesgremio.data.PlayerGuildData;
import com.mpxcosos.clasesgremio.entity.RecepcionistaEntity;
import com.mpxcosos.clasesgremio.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import com.mpxcosos.clasesgremio.registry.ModArmor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private static final double MAX_DISTANCE_SQR = 8.0 * 8.0;

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        // Los handlers del cliente van en lambdas: la clase de cliente solo se carga al ejecutarse,
        // así un servidor dedicado nunca la toca.
        registrar.playToClient(SyncGuildDataPayload.TYPE, SyncGuildDataPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientPayloadHandler.handleSync(payload)));
        registrar.playToClient(OpenRecepcionistaPayload.TYPE, OpenRecepcionistaPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientPayloadHandler.handleOpen(payload)));

        registrar.playToClient(CooldownPayload.TYPE, CooldownPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientPayloadHandler.handleCooldown(payload)));
        registrar.playToClient(FxPayload.TYPE, FxPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientPayloadHandler.handleFx(payload)));

        registrar.playToServer(ChooseClassPayload.TYPE, ChooseClassPayload.STREAM_CODEC, ModNetwork::handleChoose);
        registrar.playToServer(UseAbilityPayload.TYPE, UseAbilityPayload.STREAM_CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer sp)
                        com.mpxcosos.clasesgremio.ability.AbilityManager.use(sp, payload.slot());
                }));
    }

    public static void sync(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncGuildDataPayload(player.getData(ModAttachments.GUILD_DATA)));
    }

    private static void handleChoose(ChooseClassPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            PlayerGuildData data = player.getData(ModAttachments.GUILD_DATA);
            if (data.hasClass()) {
                player.sendSystemMessage(Component.translatable("msg.clases_gremio.ya_tienes_clase")
                        .withStyle(ChatFormatting.RED));
                sync(player);
                return;
            }

            GuildClass chosen = GuildClass.byId(payload.classId());
            if (chosen == null) return;

            Entity entity = player.level().getEntity(payload.entityId());
            if (!(entity instanceof RecepcionistaEntity recepcionista)
                    || player.distanceToSqr(recepcionista) > MAX_DISTANCE_SQR) {
                return;
            }

            player.setData(ModAttachments.GUILD_DATA, data.withClass(chosen).withRank(GuildRank.F));
            sync(player);

            // equipo inicial de la clase (etapa F-E), si ya existe
            ModArmor.ArmorSet starter = ModArmor.get(chosen, 0);
            if (starter != null) {
                for (var piece : starter.pieces()) {
                    ItemStack stack = new ItemStack(piece.get());
                    if (!player.getInventory().add(stack)) player.drop(stack, false);
                }
                var weapon = com.mpxcosos.clasesgremio.registry.ModWeapons.get(chosen, 0);
                if (weapon != null) {
                    ItemStack w = new ItemStack(weapon);
                    if (!player.getInventory().add(w)) player.drop(w, false);
                }
                player.sendSystemMessage(Component.translatable("msg.clases_gremio.equipo_inicial"));
            }

            recepcionista.playGreet();
            player.level().playSound(null, recepcionista.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.NEUTRAL, 0.8f, 1.2f);
            player.sendSystemMessage(Component.translatable("msg.clases_gremio.clase_elegida",
                    chosen.displayName().copy().withColor(chosen.color()),
                    Component.literal(GuildRank.F.letter()).withColor(GuildRank.F.color())));
        });
    }

    private ModNetwork() {
    }
}
