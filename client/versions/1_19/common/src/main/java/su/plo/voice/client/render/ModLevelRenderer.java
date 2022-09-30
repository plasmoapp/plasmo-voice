package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.event.render.LevelRenderEvent;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.lib.client.ModClientLib;
import su.plo.voice.lib.client.render.ModCamera;
import su.plo.voice.proto.data.pos.Pos3d;

import java.util.Objects;

public final class ModLevelRenderer extends ModRenderer {

    private ModCamera camera;
    private ClientLevel level;

    public ModLevelRenderer(@NotNull ModClientLib minecraft, @NotNull PlasmoVoiceClient voiceClient) {
        super(minecraft, voiceClient);
    }

    public void render(@NotNull ClientLevel level, @NotNull PoseStack poseStack, @NotNull Camera camera, float delta) {
        if (this.camera == null) {
            this.camera = new ModCamera(camera);
        }

        if (!Objects.equals(this.level, level)) {
            this.level = level;
        }

        setPoseStack(poseStack);

        voiceClient.getEventBus().call(new LevelRenderEvent(render, this.camera, this::getLight, delta));
    }

    private int getLight(@NotNull Pos3d blockPos) {
        return LevelRenderer.getLightColor(
                level,
                new BlockPos(blockPos.getX(), blockPos.getY(), blockPos.getZ())
        );
    }
}
