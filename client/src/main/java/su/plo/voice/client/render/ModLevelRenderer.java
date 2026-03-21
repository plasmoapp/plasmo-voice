package su.plo.voice.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.client.event.LevelRenderEvent;

public final class ModLevelRenderer {

    public static void render(@NotNull ClientLevel level, @NotNull PoseStack poseStack, float delta) {
        LevelRenderEvent.INSTANCE.getInvoker().onRender(level, poseStack, delta);
    }
}
