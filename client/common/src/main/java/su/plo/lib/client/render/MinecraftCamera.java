package su.plo.lib.client.render;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.pos.Pos3d;

public interface MinecraftCamera {

    @NotNull Pos3d getPosition();

    @NotNull MinecraftQuaternion getRotation();
}
