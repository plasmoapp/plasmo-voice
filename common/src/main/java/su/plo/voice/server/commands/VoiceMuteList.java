package su.plo.voice.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import su.plo.voice.server.VoiceServer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class VoiceMuteList {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("vmutelist")
                .requires(source ->
                        CommandManager.requiresPermission(source, "voice.mutelist")
                )
                .executes(ctx -> {
                    if (VoiceServer.getMuted().size() == 0) {
                        ctx.getSource().sendSuccess(
                                new TextComponent(VoiceServer.getInstance().getMessagePrefix("muted_list_empty")),
                                false
                        );
                        return 1;
                    }

                    ctx.getSource().sendSuccess(
                            new TextComponent(VoiceServer.getInstance().getMessagePrefix("muted_list")),
                            false
                    );
                    VoiceServer.getMuted().forEach((uuid, muted) -> {
                        Optional<GameProfile> gameProfile = VoiceServer.getServer().getProfileCache().get(uuid);
                        if (gameProfile.isPresent()) {
                            String expires = muted.getTo() > 0
                                    ? new SimpleDateFormat(VoiceServer.getInstance().getMessage("mute_expires_format")).format(new Date(muted.getTo()))
                                    : VoiceServer.getInstance().getMessage("mute_expires_never");
                            String reason = muted.getReason() == null
                                    ? VoiceServer.getInstance().getMessage("mute_no_reason")
                                    : muted.getReason();
                            ctx.getSource().sendSuccess(
                                    new TextComponent(VoiceServer.getInstance().getMessage("muted_list_entry")
                                            .replace("{player}", gameProfile.get().getName())
                                            .replace("{expires}", expires)
                                            .replace("{reason}", reason)),
                                    false
                            );
                        }
                    });
                    return 1;
                }));
    }
}
