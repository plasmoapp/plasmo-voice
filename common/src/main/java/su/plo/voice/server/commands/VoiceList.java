package su.plo.voice.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import su.plo.voice.server.PlayerManager;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.socket.SocketServerUDP;

import java.util.List;
import java.util.stream.Collectors;

public class VoiceList {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vlist")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.list")
                )
                .executes(ctx -> {
                    List<String> clients = SocketServerUDP.clients.keySet().stream()
                            .map(uuid -> PlayerManager.getByUUID(uuid).getGameProfile().getName())
                            .collect(Collectors.toList());

                    ctx.getSource().sendSuccess(Component.literal(
                            VoiceServer.getInstance().getMessagePrefix("list")
                                    .replace("{count}", String.valueOf(clients.size()))
                                    .replace("{online_players}", String.valueOf(VoiceServer.getServer().getPlayerCount()))
                                    .replace("{players}", String.join(", ", clients))
                    ), false);
                    return 1;
                }));
    }
}
