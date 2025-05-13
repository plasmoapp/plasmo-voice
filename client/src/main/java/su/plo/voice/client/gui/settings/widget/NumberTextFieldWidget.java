package su.plo.voice.client.gui.settings.widget;

import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.IntConfigEntry;
import su.plo.lib.mod.client.gui.components.TextFieldWidget;
import su.plo.slib.api.chat.component.McTextComponent;

import java.util.regex.Pattern;

public final class NumberTextFieldWidget extends TextFieldWidget implements UpdatableWidget {

    private static final Pattern PATTERN = Pattern.compile("[0-9]+$");

    private final IntConfigEntry entry;

    public NumberTextFieldWidget(
            @NotNull IntConfigEntry entry,
            int x,
            int y,
            int width,
            int height
    ) {
        super(x, y, width, height, McTextComponent.empty());

        this.entry = entry;

        setResponder(s -> {
            if (s.isEmpty()) {
                entry.reset();
                return;
            }

            try {
                entry.set(Math.max(entry.getMin() + 1, Integer.parseInt(s)));
            } catch (NumberFormatException ignored) {
                entry.reset();
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
    public void updateValue() {
        setValue(String.valueOf(entry.value()));
    }
}
