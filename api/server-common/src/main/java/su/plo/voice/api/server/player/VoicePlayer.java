package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.server.player.MinecraftServerPlayer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.security.PublicKey;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface VoicePlayer {

    /**
     * Sends a packet to the player via plasmo voice minecraft channel
     */
    void sendPacket(Packet<?> packet);

    /**
     * @return true if player has a voice chat
     */
    boolean hasVoiceChat();

    /**
     * @return detected player's mod loader
     */
    Optional<PlayerModLoader> getModLoader();

    /**
     * @return true if player has disabled the voice chat
     */
    boolean isVoiceDisabled();

    /**
     * @return true if player has muted the microphone in voice chat
     */
    boolean isMicrophoneMuted();

    /**
     * @return a new instance of {@link VoicePlayerInfo}
     */
    VoicePlayerInfo createPlayerInfo();

    /**
     * Gets the activation's distance by its id
     *
     * @param activationId activation id
     * @return the activation's distance or -1 if client not sent the distances
     */
    int getActivationDistanceById(@NotNull UUID activationId);

    /**
     * @return current player's active activations
     */
    Collection<ServerActivation> getActiveActivations();

    /**
     * Visualizes sphere distance if client has enabled this feature
     *
     * @param radius   sphere radius
     * @param hexColor int hex color (e.g. 0x00a000)
     */
    void visualizeDistance(int radius, int hexColor);

    /**
     * Visualizes sphere distance with color 0x00a000 if client has enabled this feature
     *
     * @param radius sphere radius
     */
    default void visualizeDistance(int radius) {
        visualizeDistance(radius, 0x00a000);
    }

    /**
     * Sends animated actionbar (like when inserting music disc in jukebox)
     * if player has Plasmo Voice installed,
     * otherwise sends default actionbar
     *
     * @param text text to send
     */
    void sendAnimatedActionBar(@NotNull MinecraftTextComponent text);

    /**
     * Gets the player's public key
     * <br/>
     * This key used for transfer AES key securely
     */
    Optional<PublicKey> getPublicKey();

    /**
     * @return wrapped Minecraft's player object
     */
    @NotNull MinecraftServerPlayer getInstance();
}
