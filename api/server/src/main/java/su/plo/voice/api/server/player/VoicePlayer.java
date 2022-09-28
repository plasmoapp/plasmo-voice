package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.entity.VoiceEntity;
import su.plo.voice.proto.data.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents "proxy" player to an actual server player object
 */
public interface VoicePlayer extends VoiceEntity {

    /**
     * Sends a packet to the player via minecraft channel
     */
    void sendPacket(Packet<?> packet);

    void sendTranslatableMessage(@NotNull String translatable, Object ...args);

    void sendMessage(@NotNull String message);

    boolean canSee(@NotNull VoicePlayer player);

    boolean hasPermission(@NotNull String permission);

    @NotNull String getNick();

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
}
