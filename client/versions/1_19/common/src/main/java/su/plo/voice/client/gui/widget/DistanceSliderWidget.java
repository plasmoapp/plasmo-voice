package su.plo.voice.client.gui.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.config.entry.IntConfigEntry;

import java.util.Comparator;

public final class DistanceSliderWidget extends AbstractSliderButton implements UpdatableWidget {

    private final ClientActivation activation;
    private final IntConfigEntry entry;

    public DistanceSliderWidget(int x,
                                int y,
                                int width,
                                ClientActivation activation,
                                IntConfigEntry entry) {
        super(
                x,
                y,
                width,
                20,
                Component.empty(),
                (float) (activation.getDistances().indexOf(entry.value())) / (float) (activation.getDistances().size() - 1)
        );

        this.activation = activation;
        this.entry = entry;

        this.updateMessage();
    }

    public void updateValue() {
        this.value = (float) (activation.getDistances().indexOf(entry.value())) / (float) (activation.getDistances().size() - 1);
        updateMessage();
    }

    private double adjust(double value) {
        return Mth.clamp(value, activation.getMinDistance(), activation.getMaxDistance());
    }

    public int getValue(double ratio) {
        double value = adjust(Mth.lerp(
                Mth.clamp(ratio, 0.0D, 1.0D),
                activation.getMinDistance(),
                activation.getMaxDistance()
        ));

        return activation.getDistances().stream()
                .min(Comparator.comparingInt(i -> Math.abs(i - (int) value)))
                .orElseGet(activation::getMinDistance);
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(String.valueOf(getValue(value))));
    }

    @Override
    protected void applyValue() {
        int value = getValue(this.value);
        entry.set(value);
    }

    @Override
    protected void renderBg(@NotNull PoseStack poseStack, @NotNull Minecraft minecraft, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = (isHoveredOrFocused() ? 2 : 1) * 20;

        float stepValue = (float) activation.getDistances().indexOf(entry.value()) / ((float) activation.getDistances().size() - 1);

        blit(poseStack, x + (int) (stepValue * (double) (width - 8)), y, 0, 46 + i, 4, 20);
        blit(poseStack, x + (int) (stepValue * (double) (width - 8)) + 4, y, 196, 46 + i, 4, 20);
    }
}
