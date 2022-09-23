package su.plo.voice.lib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.render.MinecraftMatrix;
import su.plo.lib.client.render.MinecraftQuaternion;

public final class ModMatrix implements MinecraftMatrix {

    @Setter
    @Getter
    private PoseStack poseStack;

    @Override
    public void translate(double x, double y, double z) {
        if (poseStack == null) return;
        poseStack.translate(x, y, z);
    }

    @Override
    public void scale(float x, float y, float z) {
        if (poseStack == null) return;
        poseStack.scale(x, y, z);
    }

    @Override
    public void multiply(@NotNull MinecraftQuaternion quaternion) {
        if (poseStack == null) return;
        poseStack.mulPose(((ModQuaternion) quaternion).getInstance());
    }

    @Override
    public void push() {
        if (poseStack == null) return;
        poseStack.pushPose();
    }

    @Override
    public void pop() {
        if (poseStack == null) return;
        poseStack.popPose();
    }
}
