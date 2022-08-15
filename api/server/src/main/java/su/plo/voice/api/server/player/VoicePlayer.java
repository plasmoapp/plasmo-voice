package su.plo.voice.api.server.player;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.entity.VoiceEntity;
import su.plo.voice.api.server.pos.ServerPos3d;
import su.plo.voice.proto.data.VoicePlayerInfo;
import su.plo.voice.proto.packets.Packet;

import java.util.Optional;

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

    @NotNull ServerPos3d getPosition();

    @NotNull ServerPos3d getPosition(@NotNull ServerPos3d position);

    boolean canSee(@NotNull VoicePlayer player);

    boolean hasVoiceChat();

    Optional<PlayerModLoader> getModLoader();

    VoicePlayerInfo getInfo();

    boolean isVoiceDisabled();

    boolean isMicrophoneMuted();
}
