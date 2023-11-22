package su.plo.voice.client.gui.settings.widget;

import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.mod.client.gui.components.AbstractSlider;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.voice.api.client.audio.capture.ClientActivation;

import java.util.Comparator;

public final class DistanceSliderWidget extends AbstractSlider implements UpdatableWidget {

    private final ClientActivation activation;
    private final IntConfigEntry entry;

    public DistanceSliderWidget(
            @NotNull ClientActivation activation,
            @NotNull IntConfigEntry entry,
            int x,
            int y,
            int width,
            int height
    ) {
        super(x, y, width, height);

        this.activation = activation;
        this.entry = entry;

        updateValue();
    }

    @Override
    protected void updateText() {
        this.text = McTextComponent.literal(String.valueOf(calculateValue(value)));
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

    private int calculateValue(double value) {
        double index = Mth.lerp(
                Mth.clamp(value, 0.0D, 1.0D),
                0,
                activation.getDistances().size() - 1
        );
        index = adjust(index);
        index = Math.round(index);

        return activation.getDistances().get((int) index);
    }

    private double adjust(double value) {
        return Mth.clamp(value, 0, activation.getDistances().size() - 1);
    }

}
