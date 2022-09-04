package su.plo.voice.client.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.proto.data.capture.Activation;
import su.plo.voice.proto.data.capture.VoiceActivation;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket;

import java.net.InetSocketAddress;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString
public final class VoiceServerInfo implements ServerInfo {

    @Getter
    private final UUID serverId;

    @Getter
    private final UUID secret;

    @Getter
    @Setter
    private @NotNull InetSocketAddress remoteAddress;

    @Getter
    @Setter
    private @NotNull VoiceInfo voiceInfo;

    @Getter
    @Setter
    private @NotNull PlayerInfo playerInfo;

    @Setter
    private @Nullable Encryption encryption;

    public VoiceServerInfo(@NotNull UUID serverId,
                           @NotNull UUID secret,
                           @NotNull InetSocketAddress remoteAddress,
                           @Nullable Encryption encryption,
                           @NotNull ConfigPacket config) {
        this.serverId = checkNotNull(serverId, "serverId");
        this.secret = checkNotNull(secret, "secret");
        this.encryption = encryption;
        this.remoteAddress = remoteAddress;
        this.voiceInfo = new VoiceServerVoiceInfo(
                config.getSampleRate(),
                config.getCodec(),
                config.getFadeDivisor(),
                config.getProximityActivation(),
                new ArrayList<>(config.getActivations())
        );
        this.playerInfo = new VoiceServerPlayerInfo(config.getPermissions());
    }

    @Override
    public Optional<Encryption> getEncryption() {
        return Optional.ofNullable(encryption);
    }

    @AllArgsConstructor
    @ToString
    static final class VoiceServerVoiceInfo implements ServerInfo.VoiceInfo {

        @Getter
        @Setter
        private int sampleRate;

        @Getter
        private final String codec;

        @Getter
        @Setter
        private int fadeDivisor;

        @Getter
        @Setter
        private VoiceActivation proximityActivation;

        @Setter
        private List<Activation> activations;

        @Override
        public Collection<Activation> getActivations() {
            return activations;
        }
    }

    @ToString
    static final class VoiceServerPlayerInfo implements ServerInfo.PlayerInfo {

        private final Map<String, Boolean> permissions;

        public VoiceServerPlayerInfo(@NotNull Map<String, Boolean> permissions) {
            this.permissions = checkNotNull(permissions, "permissions");
        }

        @Override
        public Optional<Boolean> get(@NotNull String key) {
            return Optional.ofNullable(permissions.get(checkNotNull(key)));
        }
    }
}
