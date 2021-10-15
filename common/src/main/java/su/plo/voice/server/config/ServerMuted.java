package su.plo.voice.server.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ServerMuted {
    private final UUID uuid;
    private final Long to;
    private final String reason;
}
