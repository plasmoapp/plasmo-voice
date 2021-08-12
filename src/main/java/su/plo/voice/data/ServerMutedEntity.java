package su.plo.voice.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ServerMutedEntity {
    private final UUID uuid;
    private final Long to;
    private final String reason;
}
