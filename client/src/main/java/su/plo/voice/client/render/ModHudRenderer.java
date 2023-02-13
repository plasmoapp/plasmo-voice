package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import gg.essential.universal.UMatrixStack;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.client.event.render.HudRenderEvent;

public final class ModHudRenderer extends ModRenderer {

    public ModHudRenderer(@NotNull PlasmoVoiceClient voiceClient) {
        super(voiceClient);
    }

    public void render(@NotNull PoseStack poseStack, float delta) {
        voiceClient.getEventBus().call(new HudRenderEvent(new UMatrixStack(poseStack), delta));
    }
}
