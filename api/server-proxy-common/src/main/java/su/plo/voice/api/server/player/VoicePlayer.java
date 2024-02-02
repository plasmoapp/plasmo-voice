package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.entity.player.McPlayer;
import su.plo.slib.api.position.Pos3d;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.security.PublicKey;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a voice API for the player.
 */
public interface VoicePlayer {

    /**
     * Sends a packet to the player via the Plasmo Voice Minecraft channel.
     *
     * @param packet The packet to send.
     */
    void sendPacket(Packet<?> packet);

    /**
     * Checks if the player has a Plasmo Voice installed and connected to the UDP server.
     *
     * @return {@code true} if the player has a Plasmo Voice installed and connected to the UDP server,
     * {@code false} otherwise.
     */
    boolean hasVoiceChat();

    /**
     * Checks if the player has voice chat disabled.
     *
     * @return true if the player has disabled voice chat.
     */
    boolean isVoiceDisabled();

    /**
     * Checks if the player has muted the microphone in voice chat.
     *
     * @return true if the player has muted the microphone.
     */
    boolean isMicrophoneMuted();

    /**
     * Creates and returns a new instance of {@link VoicePlayerInfo} for this player.
     *
     * @return A new VoicePlayerInfo instance.
     */
    VoicePlayerInfo createPlayerInfo();

    /**
     * Gets the activation's distance by its ID.
     *
     * @param activationId The activation ID.
     * @return The activation's distance or -1 if the client has not sent the distance for this activation.
     */
    int getActivationDistanceById(@NotNull UUID activationId);

    /**
     * Retrieves the currently active activations for the player.
     *
     * <p>
     *     <b>"Active"</b> means that activation is currently used by the player.
     * </p>
     *
     * @return A collection of active activations.
     */
    Collection<ServerActivation> getActiveActivations();

    /**
     * Visualizes a sphere distance if the client has enabled this feature.
     *
     * @param radius   The sphere radius.
     * @param hexColor The hexadecimal color (e.g., 0x00a000).
     */
    void visualizeDistance(int radius, int hexColor);

    /**
     * Visualizes a sphere distance with the default color (0x00a000) if the client has enabled this feature.
     *
     * @param radius The sphere radius.
     */
    default void visualizeDistance(int radius) {
        visualizeDistance(radius, 0x00a000);
    }

    /**
     * Visualizes a sphere distance at a specific position if the client has enabled this feature.
     *
     * @param position The position where the sphere should be visualized.
     * @param radius   The sphere radius.
     * @param hexColor The hexadecimal color (e.g., 0x00a000).
     */
    void visualizeDistance(@NotNull Pos3d position, int radius, int hexColor);

    /**
     * Visualizes a sphere distance at a specific position with the default color (0x00a000)
     * if the client has enabled this feature.
     *
     * @param position The position where the sphere should be visualized.
     * @param radius   The sphere radius.
     */
    default void visualizeDistance(@NotNull Pos3d position, int radius) {
        visualizeDistance(position, radius, 0x00a000);
    }

    /**
     * Sends an animated actionbar message to the player.
     * This is similar to the action when inserting a music disc in a jukebox.
     * If the player has Plasmo Voice installed, the animated actionbar will be displayed;
     * otherwise, a default actionbar will be sent.
     *
     * @param text The text to send in the animated actionbar.
     */
    void sendAnimatedActionBar(@NotNull McTextComponent text);

    /**
     * Gets the player's public key. This key is used for transferring AES keys securely.
     *
     * @return An optional containing the player's public key, or empty if not available.
     */
    Optional<PublicKey> getPublicKey();

    /**
     * Gets the Minecraft player instance associated with this voice player.
     *
     * @return The Minecraft player instance.
     */
    @NotNull McPlayer getInstance();
}
