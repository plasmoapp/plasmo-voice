package su.plo.voice.api.client.event.socket;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.event.Event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired once the UDP client changes its timed-out state.
 */
public final class UdpClientTimedOutEvent implements Event {

    @Getter
    private final UdpClient client;
    @Getter
    private final boolean timedOut;

    public UdpClientTimedOutEvent(@NotNull UdpClient client, boolean timedOut) {
        this.client = checkNotNull(client, "client cannot be null");
        this.timedOut = timedOut;
    }
}
