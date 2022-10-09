package su.plo.voice.api.client.socket;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.proto.packets.Packet;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

public interface UdpClient {

    void connect(String ip, int port);

    void close(@NotNull UdpClientClosedEvent.Reason reason);

    void sendPacket(Packet<?> packet);

    @NotNull UUID getSecret();

    Optional<InetSocketAddress> getRemoteAddress();

    boolean isClosed();

    boolean isConnected();

    boolean isTimedOut();
}
