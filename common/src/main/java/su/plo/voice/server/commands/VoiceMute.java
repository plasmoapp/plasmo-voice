package su.plo.voice.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import su.plo.voice.common.packets.tcp.ClientMutedPacket;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.config.ServerMuted;
import su.plo.voice.server.network.ServerNetworkHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoiceMute {
    private static final Pattern pattern = Pattern.compile("^([0-9]*)([smhdwu])?$");
    private static final Pattern integerPattern = Pattern.compile("^([0-9]*)$");

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vmute")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.mute")
                )
                .then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
                    PlayerList playerList = commandContext.getSource().getServer().getPlayerList();
                    return SharedSuggestionProvider.suggest(playerList.getPlayers().stream()
                            .map((serverPlayer) -> serverPlayer.getGameProfile().getName()), suggestionsBuilder);
                })
                        .executes(ctx -> {
                            GameProfileArgument.getGameProfiles(ctx, "targets").forEach(gameProfile -> {
                                mute(
                                        ctx,
                                        VoiceServer.getServer().getPlayerList().getPlayer(gameProfile.getId()),
                                        null,
                                        null
                                );
                            });

                            return 1;
                        })
                        .then(Commands.argument("duration", StringArgumentType.word()).suggests((ctx, builder) -> {
                            String arg = builder.getRemaining();
                            List<String> suggests = new ArrayList<>();
                            if (arg.isEmpty()) {
                                suggests.add("permanent");
                            } else {
                                Matcher matcher = integerPattern.matcher(arg);
                                if (matcher.find()) {
                                    suggests.add(arg + "m");
                                    suggests.add(arg + "h");
                                    suggests.add(arg + "d");
                                    suggests.add(arg + "w");
                                }
                            }

                            return SharedSuggestionProvider.suggest(suggests, builder);
                        })
                                .executes(ctx -> {
                                    GameProfileArgument.getGameProfiles(ctx, "targets").forEach(gameProfile -> {
                                        mute(
                                                ctx,
                                                VoiceServer.getServer().getPlayerList().getPlayer(gameProfile.getId()),
                                                StringArgumentType.getString(ctx, "duration"),
                                                null
                                        );
                                    });

                                    return 1;
                                })
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            GameProfileArgument.getGameProfiles(ctx, "targets").forEach(gameProfile -> {
                                                mute(
                                                        ctx,
                                                        VoiceServer.getServer().getPlayerList().getPlayer(gameProfile.getId()),
                                                        StringArgumentType.getString(ctx, "duration"),
                                                        StringArgumentType.getString(ctx, "reason")
                                                );
                                            });

                                            return 1;
                                        })))));
    }

    private static void mute(CommandContext<CommandSourceStack> ctx, ServerPlayer player, String rawDuration, String reason) {
        if (player == null) {
            ctx.getSource().sendFailure(new TextComponent(VoiceServer.getInstance().getMessagePrefix("player_not_found")));
            return;
        }

        if (VoiceServer.getMuted().containsKey(player.getUUID())) {
            ctx.getSource().sendFailure(new TextComponent(
                    VoiceServer.getInstance().getMessagePrefix("already_muted")
                            .replace("{player}", player.getGameProfile().getName())
            ));
            return;
        }

        String durationMessage = VoiceServer.getInstance().getMessage("mute_durations.permanent");
        long duration = 0;
        if (rawDuration != null) {
            if (!rawDuration.startsWith("perm")) {
                System.out.println(rawDuration);
                Matcher matcher = pattern.matcher(rawDuration);
                if (matcher.find()) {
                    duration = Integer.parseInt(matcher.group(1));
                    if (duration > 0) {
                        String type = matcher.group(2);
                        if (type == null) {
                            type = "";
                        }

                        switch (type) {
                            case "m":
                                durationMessage = String.format(VoiceServer.getInstance().getMessage("mute_durations.minutes"), duration);
                                duration *= 60;
                                break;
                            case "h":
                                durationMessage = String.format(VoiceServer.getInstance().getMessage("mute_durations.hours"), duration);
                                duration *= 3600;
                                break;
                            case "d":
                                durationMessage = String.format(VoiceServer.getInstance().getMessage("mute_durations.days"), duration);
                                duration *= 86400;
                                break;
                            case "w":
                                durationMessage = String.format(VoiceServer.getInstance().getMessage("mute_durations.weeks"), duration);
                                duration *= 604800;
                                break;
                            default:
                                durationMessage = String.format(VoiceServer.getInstance().getMessage("mute_durations.seconds"), duration);
                                break;
                        }
                    } else {
                        durationMessage = String.format(VoiceServer.getInstance().getMessage("mute_durations.seconds"), duration);
                    }
                }
            }
        }

        if (duration > 0) {
            duration *= 1000;
            duration += System.currentTimeMillis();
        }

        ServerMuted serverMuted = new ServerMuted(player.getUUID(), duration, reason);
        VoiceServer.getMuted().put(player.getUUID(), serverMuted);
        VoiceServer.saveData(true);

        ServerNetworkHandler.sendToClients(new ClientMutedPacket(serverMuted.getUuid(), serverMuted.getTo()), null);
        if (duration == 0L) {
            ctx.getSource().sendSuccess(new TextComponent(
                    VoiceServer.getInstance().getMessagePrefix("muted_perm")
                            .replace("{player}", player.getGameProfile().getName())
                            .replace("{reason}", reason != null
                                    ? reason
                                    : VoiceServer.getInstance().getMessage("mute_no_reason"))),
            false);
        } else {
            ctx.getSource().sendSuccess(new TextComponent(
                    VoiceServer.getInstance().getMessagePrefix("muted")
                            .replace("{player}", player.getGameProfile().getName())
                            .replace("{duration}", durationMessage)
                            .replace("{reason}", reason != null
                                    ? reason
                                    : VoiceServer.getInstance().getMessage("mute_no_reason"))
            ), false);
        }

        ctx.getSource().sendSuccess(new TextComponent(
                (duration > 0
                        ? VoiceServer.getInstance().getMessagePrefix("player_muted")
                        : VoiceServer.getInstance().getMessagePrefix("player_muted_perm"))
                        .replace("{duration}", durationMessage)
                        .replace("{reason}", reason != null
                                ? reason
                                : VoiceServer.getInstance().getMessage("mute_no_reason")
                        )
                ), false);
    }
}
