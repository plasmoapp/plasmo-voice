package su.plo.voice.api.server.event.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

/**
 * This event is fired when the {@link PlayerAudioPacket} is received.
 */
public final class PlayerSpeakEvent extends EventCancellableBase {

    @Getter
    private final VoicePlayer player;
    @Getter
    private final PlayerAudioPacket packet;

    @Getter
    @Setter
    private ServerActivation.Result result = ServerActivation.Result.IGNORED;

    public PlayerSpeakEvent(@NotNull VoicePlayer player,
                            @NotNull PlayerAudioPacket packet) {
        this.player = player;
        this.packet = packet;
    }
}
