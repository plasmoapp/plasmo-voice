package su.plo.voice.api.client.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.audio.source.SelfSourceInfo;

public interface ClientSelfSourceInfo {

    @NotNull SelfSourceInfo getSelfSourceInfo();

    long getSequenceNumber();

    short getDistance();

    long getLastUpdate();
}
