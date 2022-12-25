package su.plo.voice.client.audio.source;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.source.ClientSelfSourceInfo;
import su.plo.voice.proto.data.audio.source.SelfSourceInfo;

public class VoiceClientSelfSourceInfo implements ClientSelfSourceInfo {

    @Getter
    private SelfSourceInfo selfSourceInfo;
    @Getter
    private long sequenceNumber;
    @Getter
    private short distance;
    @Getter
    private long lastUpdate;

    public void setSelfSourceInfo(@NotNull SelfSourceInfo selfSourceInfo) {
        this.selfSourceInfo = selfSourceInfo;
        this.sequenceNumber = selfSourceInfo.getSequenceNumber();
        update();
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        update();
    }

    public void setDistance(short distance) {
        this.distance = distance;
        update();
    }

    private void update() {
        this.lastUpdate = System.currentTimeMillis();
    }
}
