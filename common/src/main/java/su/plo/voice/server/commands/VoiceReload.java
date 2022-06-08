package su.plo.voice.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.common.packets.tcp.ConfigPacket;
import su.plo.voice.server.PlayerManager;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.config.ServerConfig;
import su.plo.voice.server.network.ServerNetworkHandler;
import su.plo.voice.server.socket.SocketServerUDP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.UUID;

public class VoiceReload {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vreload")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.reload")
                )
                .executes(ctx -> {
                    VoiceServer.getInstance().loadConfig();
                    VoiceServer.getInstance().updateConfig();

                    ServerConfig config = VoiceServer.getServerConfig();

                    try {
                        Enumeration<UUID> it = SocketServerUDP.clients.keys();
                        while (it.hasMoreElements()) {
                            ServerPlayer player = PlayerManager.getByUUID(it.nextElement());

                            ServerNetworkHandler.sendTo(
                                    new ConfigPacket(config.getSampleRate(),
                                            new ArrayList<>(config.getDistances()),
                                            config.getDefaultDistance(),
                                            config.getMaxPriorityDistance(),
                                            config.isDisableVoiceActivation() ||
                                                    !VoiceServer.getPlayerManager().hasPermission(player.getUUID(), "voice.activation"),
                                            config.getFadeDivisor(),
                                            config.getPriorityFadeDivisor()
                                    ),
                                    player);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ctx.getSource().sendSuccess(
                            Component.literal(VoiceServer.getInstance().getMessagePrefix("reloaded")),
                            false
                    );

                    return 1;
                })
        );
    }
}
