package su.plo.voice.api.pos;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Pos3d {
    private final double x;
    private final double y;
    private final double z;
}
