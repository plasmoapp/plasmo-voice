package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.server.entity.player.McServerPlayer;

/**
 * Represents a voice API for the server player.
 */
public interface VoiceServerPlayer extends VoicePlayer {

    @NotNull McServerPlayer getInstance();
}
