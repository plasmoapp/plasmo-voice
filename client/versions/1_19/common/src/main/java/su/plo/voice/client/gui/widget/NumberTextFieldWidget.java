package su.plo.voice.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.config.entry.IntConfigEntry;

import java.util.regex.Pattern;

public final class NumberTextFieldWidget extends EditBox implements UpdatableWidget {

    private static final Pattern PATTERN = Pattern.compile("[0-9]+$");

    private final IntConfigEntry entry;

    public NumberTextFieldWidget(Font textRenderer,
                                 int x,
                                 int y,
                                 int width,
                                 int height,
                                 IntConfigEntry entry) {
        super(textRenderer, x, y, width - 3, height - 2, Component.empty());

        this.entry = entry;

        setResponder(s -> {
            if (s.isEmpty()) {
                entry.reset();
                updateValue();
                return;
            }

            try {
                entry.set(Math.max(entry.getMin() + 1, Integer.parseInt(s)));
                updateValue();
            } catch (NumberFormatException ignored) {
                entry.reset();
                updateValue();
            }
        });

        setFilter(s -> {
            if (s.isEmpty()) return true;

            if (!PATTERN.matcher(s).matches())
                return false;

            int i = Integer.parseInt(s);
            return i > 0 && i <= entry.getMax();
        });

        setValue(String.valueOf(entry.value()));
    }

    @Override
    public void renderButton(@NotNull PoseStack poseStack, int mouseX, int mouseY, float delta) {
        this.x += 1;
        this.y += 1;
        poseStack.pushPose();
        RenderSystem.enableDepthTest();
        poseStack.translate(0.0D, 0.0D, 0.0D);
        super.renderButton(poseStack, mouseX, mouseY, delta);
        poseStack.popPose();
    }

    @Override
    public void updateValue() {
        setValue(String.valueOf(entry.value()));
    }
}
