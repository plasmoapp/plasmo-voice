package su.plo.voice.proxy.server;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.proxy.server.RemoteServer;
import su.plo.voice.proxy.util.AddressUtil;

import java.net.InetSocketAddress;

@ToString
public final class VoiceRemoteServer implements RemoteServer {

    private final String name;
    private @NotNull InetSocketAddress address;
    @Getter
    @Setter
    private boolean aesEncryptionKeySet;

    public VoiceRemoteServer(@NotNull String name, @NotNull InetSocketAddress address) {
        this.name = name;
        this.address = address;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull InetSocketAddress getAddress(boolean resolve) {
        if (!resolve) {
            return address;
        }

        this.address = AddressUtil.resolveAddress(address);
        return address;
    }
}
