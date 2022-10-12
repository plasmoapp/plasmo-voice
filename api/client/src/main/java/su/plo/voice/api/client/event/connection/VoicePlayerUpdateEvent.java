package su.plo.voice.api.client.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.event.Event;
import su.plo.voice.proto.data.player.VoicePlayerInfo;

/**
 * This event is fired once the {@link su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoUpdatePacket} is received
 */
public class VoicePlayerUpdateEvent implements Event {

    @Getter
    private final VoicePlayerInfo player;

    public VoicePlayerUpdateEvent(@NotNull VoicePlayerInfo player) {
        this.player = player;
    }
}
