package su.plo.voice.data;

import java.util.List;

public class DataEntity {
    public final List<ServerMutedEntity> muted;

    public DataEntity(List<ServerMutedEntity> muted) {
        this.muted = muted;
    }
}
