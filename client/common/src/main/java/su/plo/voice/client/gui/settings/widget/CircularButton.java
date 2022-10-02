package su.plo.voice.client.gui.settings.widget;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.chat.TextComponent;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.components.Button;

import java.util.List;

public final class CircularButton extends Button implements UpdatableWidget {

    private final List<TextComponent> values;
    private final @Nullable UpdateAction updateAction;
    @Setter
    private int index;

    public CircularButton(@NotNull MinecraftClientLib minecraft,
                          @NotNull List<TextComponent> values,
                          int index,
                          int x,
                          int y,
                          int width,
                          int height,
                          @Nullable UpdateAction updateAction,
                          @NotNull OnTooltip tooltipAction) {
        super(minecraft, x, y, width, height, TextComponent.empty(), Button.NO_ACTION, tooltipAction);

        this.values = values;
        this.index = index;
        this.updateAction = updateAction;
        updateValue();
    }

    @Override
    public void updateValue() {
        setText(values.get(index));
    }

    @Override
    public void onPress() {
        super.onPress();

        this.index = (index + 1) % values.size();
        updateValue();
        if (updateAction != null) updateAction.onUpdate(index);
    }

    public interface UpdateAction {

        void onUpdate(int index);
    }
}
