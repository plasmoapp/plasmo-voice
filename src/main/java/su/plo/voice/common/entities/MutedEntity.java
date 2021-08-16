package su.plo.voice.common.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class MutedEntity {
    private UUID uuid;
    private Long to;
}
