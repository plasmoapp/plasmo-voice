package su.plo.voice.data;

import java.util.List;
import java.util.UUID;

public class DataEntity {
    public final List<UUID> mutedClients;

    public DataEntity(List<UUID> mutedClients) {
        this.mutedClients = mutedClients;
    }
}
