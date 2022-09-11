package su.plo.voice.client.gui.widget;

import lombok.Setter;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CircularButton extends Button implements UpdatableWidget {

    private final List<Component> values;
    private final @Nullable UpdateAction updateAction;
    @Setter
    private int index;

    public CircularButton(int x,
                          int y,
                          int width,
                          int height,
                          List<Component> values,
                          int index,
                          @Nullable UpdateAction updateAction,
                          OnTooltip onTooltip) {
        super(x, y, width, height, Component.empty(), (button) -> {}, onTooltip);

        this.updateAction = updateAction;
        this.values = values;
        this.index = index;

        updateValue();
    }

    @Override
    public void updateValue() {
        setMessage(values.get(index));
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
