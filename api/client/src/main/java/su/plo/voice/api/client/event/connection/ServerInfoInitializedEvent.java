package su.plo.voice.api.client.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.event.Event;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the {@link ConfigPacket} is received
 * and {@link ServerInfo} is available in {@link PlasmoVoiceClient#getServerInfo()}.
 */
public final class ServerInfoInitializedEvent implements Event {

    @Getter
    private final ServerInfo serverInfo;
    @Getter
    private final ConfigPacket packet;

    public ServerInfoInitializedEvent(@NotNull ServerInfo serverInfo,
                                      @NotNull ConfigPacket packet) {
        this.serverInfo = checkNotNull(serverInfo, "serverInfo cannot be null");
        this.packet = checkNotNull(packet, "packet cannot be null");
    }
}
