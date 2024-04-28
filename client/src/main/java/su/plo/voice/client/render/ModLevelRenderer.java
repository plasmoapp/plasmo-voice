package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import su.plo.slib.api.position.Pos3d;
import gg.essential.universal.UMatrixStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.client.event.render.LevelRenderEvent;
import su.plo.voice.api.client.PlasmoVoiceClient;

import java.util.Objects;

public final class ModLevelRenderer extends ModRenderer {

    private ClientLevel level;

    public ModLevelRenderer(@NotNull PlasmoVoiceClient voiceClient) {
        super(voiceClient);
    }

    public void render(@NotNull ClientLevel level, @NotNull PoseStack poseStack, @NotNull Camera camera, float delta) {
        if (!Objects.equals(this.level, level)) {
            this.level = level;
        }

        voiceClient.getEventBus().fire(new LevelRenderEvent(
                new UMatrixStack(poseStack),
                level,
                new ModCamera(camera.getPosition(), camera.getXRot(), camera.getYRot()),
                this::getLight,
                delta
        ));
    }

    private int getLight(@NotNull Pos3d blockPos) {
        return LevelRenderer.getLightColor(
                level,
                new BlockPos((int) blockPos.getX(), (int) blockPos.getY(), (int) blockPos.getZ())
        );
    }
}
