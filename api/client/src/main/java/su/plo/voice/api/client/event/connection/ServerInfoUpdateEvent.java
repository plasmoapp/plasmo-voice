package su.plo.voice.api.client.event.connection;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.event.Event;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the {@link ConfigPacket} is received
 * and {@link ServerInfo} is available in {@link PlasmoVoiceClient#getCurrentServerInfo()}
 */
public final class ServerInfoUpdateEvent implements Event {

    private final ServerInfo oldServerInfo;

    @Getter
    private final ServerInfo currentServerInfo;

    @Getter
    private final ConfigPacket packet;

    public ServerInfoUpdateEvent(@Nullable ServerInfo oldServerInfo,
                                 @NotNull ServerInfo currentServerInfo,
                                 @NotNull ConfigPacket packet) {
        this.oldServerInfo = oldServerInfo;
        this.currentServerInfo = checkNotNull(currentServerInfo, "currentServerInfo cannot be null");
        this.packet = checkNotNull(packet, "packet cannot be null");
    }

    public Optional<ServerInfo> getOldServerInfo() {
        return Optional.ofNullable(oldServerInfo);
    }
}
