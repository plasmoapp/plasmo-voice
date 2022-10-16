package su.plo.voice.mod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.client.event.render.HudRenderEvent;
import su.plo.lib.mod.client.ModClientLib;
import su.plo.voice.api.client.PlasmoVoiceClient;

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
