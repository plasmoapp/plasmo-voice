package su.plo.lib.client.render;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.Camera;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.data.pos.Pos3d;

@RequiredArgsConstructor
public final class ModCamera implements MinecraftCamera {

    @Getter
    private final Camera camera;

    private final Pos3d position = new Pos3d();
    private final ModQuaternion quaternion = new ModQuaternion();

    @Override
    public @NotNull Pos3d getPosition() {
        position.setX(camera.getPosition().x());
        position.setY(camera.getPosition().y());
        position.setZ(camera.getPosition().z());

        return position;
    }

    @Override
    public @NotNull MinecraftQuaternion getRotation() {
        if (quaternion.getInstance() == null) quaternion.setInstance(camera.rotation());
        return quaternion;
    }
}
