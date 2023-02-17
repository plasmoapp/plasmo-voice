package su.plo.voice.api.proxy.player;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.proxy.player.MinecraftProxyPlayer;
import su.plo.voice.api.server.player.VoicePlayer;

public interface VoiceProxyPlayer extends VoicePlayer {

    @NotNull MinecraftProxyPlayer getInstance();
}
