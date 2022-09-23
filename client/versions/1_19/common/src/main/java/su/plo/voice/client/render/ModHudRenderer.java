package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.event.render.HudRenderEvent;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.lib.client.ModClientLib;

public final class ModHudRenderer extends ModRenderer {

    public ModHudRenderer(@NotNull ModClientLib minecraft,
                          @NotNull PlasmoVoiceClient voiceClient) {
        super(minecraft, voiceClient);
    }

    public void render(@NotNull PoseStack poseStack, float delta) {
        setPoseStack(poseStack);

        voiceClient.getEventBus().call(new HudRenderEvent(render, delta));
    }
}
