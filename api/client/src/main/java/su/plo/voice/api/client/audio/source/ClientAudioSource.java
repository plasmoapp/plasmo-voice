package su.plo.voice.api.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.source.AudioSource;
import su.plo.voice.api.client.audio.device.DeviceException;
import su.plo.voice.proto.data.audio.source.SourceInfo;
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket;
import su.plo.voice.proto.packets.udp.cllientbound.SourceAudioPacket;

public interface ClientAudioSource<S extends SourceInfo> extends AudioSource {

    void initialize(S sourceInfo) throws DeviceException;

    @NotNull S getInfo();

    void process(@NotNull SourceAudioPacket packet);

    void process(@NotNull SourceAudioEndPacket packet);

    boolean isClosed();

    boolean isActivated();

    void close();
}
