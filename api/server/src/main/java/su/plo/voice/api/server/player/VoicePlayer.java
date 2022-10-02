package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.server.entity.MinecraftServerPlayer;
import su.plo.voice.proto.data.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents "proxy" player to an actual server player object
 */
public interface VoicePlayer {

    /**
     * Sends a packet to the player via minecraft channel
     */
    void sendPacket(Packet<?> packet);

    boolean hasVoiceChat();

    Optional<PlayerModLoader> getModLoader();

    VoicePlayerInfo getInfo();

    boolean isVoiceDisabled();

    boolean isMicrophoneMuted();

    /**
     * Gets the activation's distance by its id
     *
     * @param activationId activation id
     *
     * @return the activation's distance or -1 if client not sent the distances
     */
    int getActivationDistanceById(@NotNull UUID activationId);

    void visualizeDistance(int radius, int hexColor);

    void visualizeDistance(int radius);

    MinecraftServerPlayer getInstance();
}
