package su.plo.voice.api.client.event.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.player.VoicePlayerInfo;
import su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoUpdatePacket;

/**
 * This event is fired once the {@link PlayerInfoUpdatePacket} is received for the first time.
 */
public final class VoicePlayerConnectedEvent extends VoicePlayerUpdateEvent {

    public VoicePlayerConnectedEvent(@NotNull VoicePlayerInfo player) {
        super(player);
    }
}
