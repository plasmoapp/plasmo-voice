package su.plo.voice.client.gui.settings.widget;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.MathLib;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.components.AbstractSlider;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.config.entry.IntConfigEntry;

import java.util.Comparator;

public final class DistanceSliderWidget extends AbstractSlider implements UpdatableWidget {

    private final ClientActivation activation;
    private final IntConfigEntry entry;

    public DistanceSliderWidget(@NotNull MinecraftClientLib minecraft,
                                @NotNull ClientActivation activation,
                                @NotNull IntConfigEntry entry,
                                int x,
                                int y,
                                int width,
                                int height) {
        super(minecraft, x, y, width, height);

        this.activation = activation;
        this.entry = entry;

        updateValue();
    }

    @Override
    protected void updateText() {
        this.text = TextComponent.literal(String.valueOf(calculateValue(value)));
    }

    @Override
    protected void applyValue() {
        entry.set(calculateValue(this.value));
        updateValue();
    }

    @Override
    public void updateValue() {
        this.value = (float) (activation.getDistances().indexOf(entry.value())) / (float) (activation.getDistances().size() - 1);
        updateText();
    }

    private int calculateValue(double ratio) {
        double value = adjust(MathLib.lerp(
                MathLib.clamp(ratio, 0.0D, 1.0D),
                activation.getMinDistance(),
                activation.getMaxDistance()
        ));

        return activation.getDistances().stream()
                .min(Comparator.comparingInt(i -> Math.abs(i - (int) value)))
                .orElseGet(activation::getMinDistance);
    }

    private double adjust(double value) {
        return MathLib.clamp(value, activation.getMinDistance(), activation.getMaxDistance());
    }

}
