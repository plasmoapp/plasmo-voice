package su.plo.voice.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.network.ServerNetworkHandler;

public class VoiceReconnect {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vrc")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.reconnect")
                )
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(Component.literal(
                            VoiceServer.getInstance().getMessagePrefix("reconnect_sent")
                    ), false);
                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                    ServerNetworkHandler.reconnectClient(player);
                    return 1;
                }));
    }
}
