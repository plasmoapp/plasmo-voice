package su.plo.voice.client.connection;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.connection.UdpClientManager;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.socket.UdpClient;

import java.util.Optional;

public final class VoiceUdpClientManager implements UdpClientManager {

    private UdpClient client;

    @Override
    public void setClient(@NotNull UdpClient client) {
        this.client = client;
    }

    @Override
    public void removeClient(@NotNull UdpClientClosedEvent.Reason reason) {
        if (client != null) client.close(reason);
        this.client = null;
    }

    @Override
    public Optional<UdpClient> getClient() {
        return Optional.ofNullable(client);
    }

    @Override
    public boolean isConnected() {
        if (client == null) return false;
        return !client.isClosed() && client.isConnected() && !client.isTimedOut();
    }
}
