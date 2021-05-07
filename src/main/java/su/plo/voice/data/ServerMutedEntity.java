package su.plo.voice.data;

import java.util.UUID;

public class ServerMutedEntity {
    public final UUID uuid;
    public final Long to;
    public final String reason;

    public ServerMutedEntity(UUID uuid, Long to, String reason) {
        this.uuid = uuid;
        this.to = to;
        this.reason = reason;
    }
}
