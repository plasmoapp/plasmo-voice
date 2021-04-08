package su.plo.voice.data;

import su.plo.voice.common.entities.MutedEntity;

import java.util.List;

public class DataEntity {
    public final List<MutedEntity> muted;

    public DataEntity(List<MutedEntity> muted) {
        this.muted = muted;
    }
}
