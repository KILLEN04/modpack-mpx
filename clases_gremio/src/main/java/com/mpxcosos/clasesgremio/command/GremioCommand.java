package com.mpxcosos.clasesgremio.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.data.GuildRank;
import com.mpxcosos.clasesgremio.data.PlayerGuildData;
import com.mpxcosos.clasesgremio.entity.RecepcionistaEntity;
import com.mpxcosos.clasesgremio.network.ModNetwork;
import com.mpxcosos.clasesgremio.registry.ModAttachments;
import com.mpxcosos.clasesgremio.registry.ModEntities;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobSpawnType;

import java.util.Arrays;

/**
 * /gremio info [jugador]
 * /gremio clase set <jugador> <clase>      (admin)
 * /gremio clase reset <jugador>            (admin)
 * /gremio rango set <jugador> <F..S>       (admin)
 * /gremio recepcionista                    (admin: la pone donde estás)
 */
public final class GremioCommand {
    private static final SimpleCommandExceptionType UNKNOWN_CLASS =
            new SimpleCommandExceptionType(Component.translatable("cmd.clases_gremio.clase_desconocida"));
    private static final SimpleCommandExceptionType UNKNOWN_RANK =
            new SimpleCommandExceptionType(Component.translatable("cmd.clases_gremio.rango_desconocido"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gremio")
                .then(Commands.literal("info")
                        .executes(ctx -> info(ctx, ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("jugador", EntityArgument.player())
                                .requires(s -> s.hasPermission(2))
                                .executes(ctx -> info(ctx, EntityArgument.getPlayer(ctx, "jugador")))))
                .then(Commands.literal("clase")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .then(Commands.argument("clase", StringArgumentType.word())
                                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                        Arrays.stream(GuildClass.values()).map(GuildClass::id), b))
                                                .executes(GremioCommand::setClass))))
                        .then(Commands.literal("reset")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .executes(GremioCommand::resetClass))))
                .then(Commands.literal("rango")
                        .requires(s -> s.hasPermission(2))
                        .then(Commands.literal("set")
                                .then(Commands.argument("jugador", EntityArgument.player())
                                        .then(Commands.argument("rango", StringArgumentType.word())
                                                .suggests((c, b) -> SharedSuggestionProvider.suggest(
                                                        Arrays.stream(GuildRank.values()).map(GuildRank::letter), b))
                                                .executes(GremioCommand::setRank)))))
                .then(Commands.literal("recepcionista")
                        .requires(s -> s.hasPermission(2))
                        .executes(GremioCommand::spawnRecepcionista)));
    }

    private static int info(CommandContext<CommandSourceStack> ctx, ServerPlayer target) {
        PlayerGuildData data = target.getData(ModAttachments.GUILD_DATA);
        GuildClass c = data.guildClass();
        GuildRank r = data.guildRank();
        Component clase = c == null
                ? Component.translatable("cmd.clases_gremio.sin_clase")
                : c.displayName().copy().withColor(c.color());
        ctx.getSource().sendSuccess(() -> Component.translatable("cmd.clases_gremio.info",
                target.getDisplayName(), clase, Component.literal(r.letter()).withColor(r.color())), false);
        return 1;
    }

    private static int setClass(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jugador");
        GuildClass c = GuildClass.byId(StringArgumentType.getString(ctx, "clase"));
        if (c == null) throw UNKNOWN_CLASS.create();
        PlayerGuildData data = target.getData(ModAttachments.GUILD_DATA);
        target.setData(ModAttachments.GUILD_DATA, data.withClass(c));
        ModNetwork.sync(target);
        return info(ctx, target);
    }

    private static int resetClass(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jugador");
        target.setData(ModAttachments.GUILD_DATA, PlayerGuildData.EMPTY);
        ModNetwork.sync(target);
        return info(ctx, target);
    }

    private static int setRank(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "jugador");
        GuildRank r = GuildRank.byLetter(StringArgumentType.getString(ctx, "rango"));
        if (r == null) throw UNKNOWN_RANK.create();
        PlayerGuildData data = target.getData(ModAttachments.GUILD_DATA);
        target.setData(ModAttachments.GUILD_DATA, data.withRank(r));
        ModNetwork.sync(target);
        return info(ctx, target);
    }

    private static int spawnRecepcionista(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        RecepcionistaEntity npc = ModEntities.RECEPCIONISTA.get().spawn(level, player.blockPosition(), MobSpawnType.COMMAND);
        if (npc != null) {
            float yaw = player.getYRot() + 180.0f;
            npc.moveTo(player.getX(), player.getY(), player.getZ(), yaw, 0.0f);
            npc.setYHeadRot(yaw);
            npc.setYBodyRot(yaw);
        }
        ctx.getSource().sendSuccess(() -> Component.translatable("cmd.clases_gremio.recepcionista_creada"), true);
        return 1;
    }

    private GremioCommand() {
    }
}
