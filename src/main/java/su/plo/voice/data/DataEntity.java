package su.plo.voice.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DataEntity {
    private final List<ServerMutedEntity> muted;
}
