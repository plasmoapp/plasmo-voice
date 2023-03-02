package su.plo.voice.client.connection;

import com.google.common.collect.Maps;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.util.Params;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.capture.CaptureInfo;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.data.audio.line.SourceLine;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket;

import javax.sound.sampled.AudioFormat;
import java.net.InetSocketAddress;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString
public final class VoiceServerInfo implements ServerInfo {

    private final PlasmoVoiceClient voiceClient;

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

    public VoiceServerInfo(@NonNull PlasmoVoiceClient voiceClient,
                           @NonNull UUID serverId,
                           @NonNull UUID secret,
                           @NonNull InetSocketAddress remoteAddress,
                           @Nullable Encryption encryption,
                           @NonNull ConfigPacket config) {
        this.voiceClient = voiceClient;
        this.serverId = serverId;
        this.secret = secret;
        this.encryption = encryption;
        this.remoteAddress = remoteAddress;
        this.voiceInfo = new VoiceServerVoiceInfo(
                config.getCodec(),
                new ArrayList<>(config.getSourceLines()),
                new ArrayList<>(config.getActivations())
        );
        this.playerInfo = new VoiceServerPlayerInfo(config.getPermissions());
    }

    @Override
    public Optional<Encryption> getEncryption() {
        return Optional.ofNullable(encryption);
    }

    @Override
    public @NotNull AudioEncoder createOpusEncoder(boolean stereo) {
        CodecInfo codecInfo = voiceInfo.getCapture().getCodec();
        if (codecInfo == null)
            throw new IllegalStateException("server codec info is empty");

        int sampleRate = voiceInfo.getCapture().getSampleRate();

        return voiceClient.getCodecManager().createEncoder(
                "opus",
                sampleRate,
                stereo,
                (sampleRate / 1_000) * 20,
                voiceInfo.getCapture().getMtuSize(),
                Params.builder().putAll(codecInfo.getParams()).build()
        );
    }

    @Override
    public @NotNull AudioEncoder createOpusDecoder(boolean stereo) {
        if (voiceInfo.getCapture().getCodec() == null)
            throw new IllegalStateException("server codec info is empty");

        int sampleRate = voiceInfo.getCapture().getSampleRate();

        return voiceClient.getCodecManager().createDecoder(
                "opus",
                sampleRate,
                stereo,
                (sampleRate / 1_000) * 20,
                voiceInfo.getCapture().getMtuSize(),
                Params.EMPTY
        );
    }

    @AllArgsConstructor
    @ToString
    @Data
    static final class VoiceServerVoiceInfo implements ServerInfo.VoiceInfo {

        private CaptureInfo capture;
        private List<SourceLine> sourceLines;
        private List<Activation> activations;

        @Override
        public @NotNull AudioFormat getFormat(boolean stereo) {
            return new AudioFormat(
                    (float) capture.getSampleRate(),
                    16,
                    stereo ? 2 : 1,
                    true,
                    false
            );
        }

        @Override
        public int getBufferSize() {
            return (capture.getSampleRate() / 1_000) * 20;
        }
    }

    @ToString
    static final class VoiceServerPlayerInfo implements ServerInfo.PlayerInfo {

        private final Map<String, Boolean> permissions = Maps.newConcurrentMap();

        public VoiceServerPlayerInfo(@NotNull Map<String, Boolean> permissions) {
            this.permissions.putAll(checkNotNull(permissions, "permissions"));
        }

        @Override
        public Optional<Boolean> get(@NotNull String key) {
            return Optional.ofNullable(permissions.get(checkNotNull(key)));
        }

        public void update(@NotNull Map<String, Boolean> permissions) {
            this.permissions.putAll(permissions);
        }
    }
}
