package su.plo.voice.api.proxy.player;

import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.proxy.player.McProxyPlayer;
import su.plo.voice.api.server.player.VoicePlayer;

/**
 * Represents a voice API for the proxy player.
 */
public interface VoiceProxyPlayer extends VoicePlayer {

    @NotNull McProxyPlayer getInstance();
}
