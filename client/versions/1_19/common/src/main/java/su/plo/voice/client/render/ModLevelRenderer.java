package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.event.render.LevelRenderEvent;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.lib.client.ModClientLib;
import su.plo.voice.lib.client.render.ModCamera;

public final class ModLevelRenderer extends ModRenderer {

    private ModCamera camera;

    public ModLevelRenderer(@NotNull ModClientLib minecraft, @NotNull PlasmoVoiceClient voiceClient) {
        super(minecraft, voiceClient);
    }

    public void render(@NotNull PoseStack poseStack, @NotNull Camera camera, float delta) {
        if (this.camera == null) {
            this.camera = new ModCamera(camera);
        }

        setPoseStack(poseStack);

        voiceClient.getEventBus().call(new LevelRenderEvent(render, this.camera, delta));
    }
}
