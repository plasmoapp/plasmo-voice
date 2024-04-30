package su.plo.voice.client.render;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.world.phys.Vec3;

@RequiredArgsConstructor
@Data
@Accessors(fluent = true)
public final class ModCamera {

    private final Vec3 position;
    private final float xRot;
    private final float yRot;
}
