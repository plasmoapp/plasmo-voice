package su.plo.voice.lib.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import lombok.Getter;
import lombok.Setter;
import su.plo.lib.client.render.MinecraftMatrix;

public class ModMatrix implements MinecraftMatrix {

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
    public void multiply(float x, float y, float z, float w) {
        if (poseStack == null) return;
        poseStack.mulPose(new Quaternion(x, y, z, w));
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
