package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.ModClientLib;
import su.plo.lib.client.gui.ModGuiRender;
import su.plo.voice.api.client.PlasmoVoiceClient;

public abstract class ModRenderer {

    protected final PlasmoVoiceClient voiceClient;

    protected final ModGuiRender render;


    public ModRenderer(@NotNull ModClientLib minecraft,
                       @NotNull PlasmoVoiceClient voiceClient) {
        this.voiceClient = voiceClient;

        this.render = new ModGuiRender(minecraft.getTesselator(), minecraft.getTextConverter(), minecraft.getResources());
    }

    protected void setPoseStack(@NotNull PoseStack poseStack) {
        render.getMatrix().setPoseStack(poseStack);
    }

    protected void setMultiBufferSource(@NotNull MultiBufferSource bufferSource) {
        render.setMultiBufferSource(bufferSource);
    }
}
