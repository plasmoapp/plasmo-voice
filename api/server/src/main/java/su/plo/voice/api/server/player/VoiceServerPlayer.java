package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.server.entity.MinecraftServerPlayerEntity;

public interface VoiceServerPlayer extends VoicePlayer {

    /**
     * @return wrapped Minecraft's player object
     */
    @NotNull MinecraftServerPlayerEntity getInstance();
}
