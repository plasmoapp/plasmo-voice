package su.plo.voice.api.client.event.connection;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.Event;

import java.security.KeyPair;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event is fired when the {@link ServerConnection} is created and the keypair has been generated but not set.
 */
public final class ConnectionKeyPairGenerateEvent implements Event {

    @Getter
    @Setter
    private @NotNull KeyPair keyPair;

    public ConnectionKeyPairGenerateEvent(@NotNull KeyPair keyPair) {
        this.keyPair = checkNotNull(keyPair, "keyPair");
    }
}
