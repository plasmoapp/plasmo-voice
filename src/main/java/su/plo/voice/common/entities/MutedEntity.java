package su.plo.voice.common.entities;

import java.util.UUID;

public class MutedEntity {
    public final UUID uuid;
    public final Long to;

    public MutedEntity(UUID uuid, Long to) {
        this.uuid = uuid;
        this.to = to;
    }
}
