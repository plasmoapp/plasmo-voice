package su.plo.voice.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import su.plo.voice.server.VoiceServer;

public class CommandManager {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        VoiceList.register(dispatcher);
        VoiceReconnect.register(dispatcher);
        VoiceReload.register(dispatcher);

        VoiceMute.register(dispatcher);
        VoiceMuteList.register(dispatcher);
        VoiceUnmute.register(dispatcher);
        VoicePermissions.register(dispatcher);
    }

    public static boolean requiresPermission(CommandSourceStack source, String permission) {
        if (source.getEntity() == null) {
            return true;
        }

        try {
            return VoiceServer.getPlayerManager().hasPermission(source.getPlayerOrException().getUUID(), permission);
        } catch (CommandSyntaxException ignored) {
            return false;
        }
    }
}
