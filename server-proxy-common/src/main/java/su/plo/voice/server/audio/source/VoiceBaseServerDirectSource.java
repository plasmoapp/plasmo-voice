package su.plo.voice.server.audio.source;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.position.Pos3d;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.PlasmoBaseVoiceServer;
import su.plo.voice.api.server.audio.line.BaseServerSourceLine;
import su.plo.voice.api.server.audio.provider.AudioFrameProvider;
import su.plo.voice.api.server.audio.source.AudioSender;
import su.plo.voice.api.server.audio.source.BaseServerDirectSource;
import su.plo.voice.api.server.connection.UdpConnectionManager;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.data.audio.source.DirectSourceInfo;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;

import java.util.UUID;

public abstract class VoiceBaseServerDirectSource
        extends BaseServerAudioSource<DirectSourceInfo>
        implements BaseServerDirectSource
{

    protected final PlasmoBaseVoiceServer voiceServer;
    protected final UdpConnectionManager<? extends VoicePlayer, ? extends UdpConnection> udpConnections;

    private @Nullable VoicePlayer sender;
    private @Nullable Pos3d relativePosition;
    private @Nullable Pos3d lookAngle;
    private boolean cameraRelative = true;

    @Setter
    @Getter
    private int angle;

    public VoiceBaseServerDirectSource(
            @NotNull PlasmoBaseVoiceServer voiceServer,
            @NotNull UdpConnectionManager<? extends VoicePlayer, ? extends UdpConnection> udpConnections,
            @NotNull AddonContainer addon,
            @NotNull BaseServerSourceLine line,
            @Nullable CodecInfo decoderInfo,
            boolean stereo
    ) {
        super(addon, UUID.randomUUID(), line, decoderInfo, stereo);

        this.voiceServer = voiceServer;
        this.udpConnections = udpConnections;
    }

    @Override
    public @Nullable VoicePlayer getSender() {
        return sender;
    }

    @Override
    public void setSender(@Nullable VoicePlayer player) {
        this.sender = player;
        updateSourceInfo();
    }

    @Override
    public @Nullable Pos3d getRelativePosition() {
        return relativePosition;
    }

    @Override
    public void setRelativePosition(@Nullable Pos3d position) {
        this.relativePosition = position;
        updateSourceInfo();
    }

    @Override
    public @Nullable Pos3d getLookAngle() {
        return lookAngle;
    }

    @Override
    public void setLookAngle(@NotNull Pos3d position) {
        this.lookAngle = position;
        updateSourceInfo();
    }

    @Override
    public boolean isCameraRelative() {
        return cameraRelative;
    }

    @Override
    public void setCameraRelative(boolean cameraRelative) {
        this.cameraRelative = cameraRelative;
        updateSourceInfo();
    }

    @Override
    public @NotNull DirectSourceInfo getSourceInfo() {
        return new DirectSourceInfo(
                addon.getId(),
                id,
                line.getId(),
                name,
                (byte) state.get(),
                decoderInfo,
                stereo,
                iconVisible,
                angle,
                sender == null ? null : sender.getInstance().getGameProfile(),
                relativePosition,
                lookAngle,
                cameraRelative
        );
    }

    @Override
    public @NotNull AudioSender createAudioSender(@NotNull AudioFrameProvider frameProvider) {
        return new AudioSender(frameProvider, this::sendAudioFrame, this::sendAudioEnd);
    }

    protected void updateSourceInfo() {
        sendPacket(new SourceInfoPacket(getSourceInfo()));
    }
}
