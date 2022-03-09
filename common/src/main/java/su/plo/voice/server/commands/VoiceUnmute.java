package su.plo.voice.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import su.plo.voice.common.packets.tcp.ClientUnmutedPacket;
import su.plo.voice.server.PlayerManager;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.config.ServerMuted;
import su.plo.voice.server.network.ServerNetworkHandler;

public class VoiceUnmute {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vunmute")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.unmute")
                )
                .then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandContext, suggestionsBuilder) -> {
                            PlayerList playerList = commandContext.getSource().getServer().getPlayerList();
                            return SharedSuggestionProvider.suggest(playerList.getPlayers().stream()
                                    .map((serverPlayer) -> serverPlayer.getGameProfile().getName()), suggestionsBuilder);
                        })
                        .executes(ctx -> {
                            GameProfileArgument.getGameProfiles(ctx, "targets").forEach(gameProfile -> {
                                unmute(ctx, gameProfile);
                            });

                            return 1;
                        })));
    }

    private static void unmute(CommandContext<CommandSourceStack> ctx, GameProfile profile) {
        ServerMuted muted = VoiceServer.getMuted().get(profile.getId());
        if (muted == null) {
            ctx.getSource().sendFailure(new TextComponent(
                    VoiceServer.getInstance().getMessagePrefix("not_muted")
                            .replace("{player}", profile.getName())
            ));
            return;
        }

        if (muted.getTo() > 0 && muted.getTo() < System.currentTimeMillis()) {
            VoiceServer.getMuted().remove(muted.getUuid());
            ctx.getSource().sendFailure(new TextComponent(
                    VoiceServer.getInstance().getMessagePrefix("not_muted")
                            .replace("{player}", profile.getName())
            ));
            return;
        }

        VoiceServer.getMuted().remove(muted.getUuid());
        VoiceServer.saveData(true);

        ServerPlayer player = PlayerManager.getByUUID(profile.getId());
        if (player != null) {
            ServerNetworkHandler.sendToClients(new ClientUnmutedPacket(profile.getId()), null);
        }

        ctx.getSource().sendSuccess(
                new TextComponent(
                        VoiceServer.getInstance().getMessagePrefix("unmuted")
                                .replace("{player}", profile.getName())
                ),
                false
        );
    }
}
