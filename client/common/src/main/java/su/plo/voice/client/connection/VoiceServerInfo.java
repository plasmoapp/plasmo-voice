package su.plo.voice.client.connection;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.proto.data.EncryptionInfo;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket;

import java.net.InetSocketAddress;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceServerInfo implements ServerInfo {

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
    private @Nullable EncryptionInfo encryptionInfo;

    public VoiceServerInfo(@NotNull UUID secret,
                           @NotNull InetSocketAddress remoteAddress,
                           @Nullable EncryptionInfo encryptionInfo,
                           @NotNull ConfigPacket config) {
        this.secret = checkNotNull(secret, "secret");
        this.encryptionInfo = encryptionInfo;
        this.remoteAddress = remoteAddress;
        this.voiceInfo = new VoiceServerVoiceInfo(
                config.getSampleRate(),
                config.getDistances(),
                config.getMaxPriorityDistance()
        );
        this.playerInfo = new VoiceServerPlayerInfo(config.getPlayerInfo());
    }

    @Override
    public Optional<EncryptionInfo> getEncryptionInfo() {
        return Optional.ofNullable(encryptionInfo);
    }

    static final class VoiceServerVoiceInfo implements ServerInfo.VoiceInfo {

        @Getter
        @Setter
        private int sampleRate;

        @Setter
        private @NotNull List<Integer> distances;

        @Getter
        @Setter
        private int maxPriorityDistance;

        VoiceServerVoiceInfo(int sampleRate, List<Integer> distances, int maxPriorityDistance) {
            this.sampleRate = sampleRate;
            this.distances = checkNotNull(distances, "distances");
            this.maxPriorityDistance = maxPriorityDistance;
        }

        @Override
        public Collection<Integer> getDistances() {
            return distances;
        }

        @Override
        public int getMinDistance() {
            return distances.get(0);
        }

        @Override
        public int getMaxDistance() {
            return distances.get(distances.size() - 1);
        }
    }

    static final class VoiceServerPlayerInfo implements ServerInfo.PlayerInfo {

        private final Map<String, Integer> playerInfo;

        public VoiceServerPlayerInfo(@NotNull Map<String, Integer> playerInfo) {
            this.playerInfo = checkNotNull(playerInfo, "playerInfo");
        }

        @Override
        public Optional<Integer> get(@NotNull String key) {
            return Optional.ofNullable(playerInfo.get(checkNotNull(key)));
        }
    }
}
