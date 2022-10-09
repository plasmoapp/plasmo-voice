package su.plo.voice.api.server.event.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.EventCancellableBase;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;

/**
 * This event is fired when the {@link PlayerAudioEndPacket} is received
 */
public final class PlayerSpeakEndEvent extends EventCancellableBase {

    @Getter
    private final VoicePlayer player;
    @Getter
    private final PlayerAudioEndPacket packet;

    public PlayerSpeakEndEvent(@NotNull VoicePlayer player,
                               @NotNull PlayerAudioEndPacket packet) {
        this.player = player;
        this.packet = packet;
    }
}
