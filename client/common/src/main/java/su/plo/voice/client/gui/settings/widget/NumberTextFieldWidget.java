package su.plo.voice.client.gui.settings.widget;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.TextComponent;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.GuiRender;
import su.plo.lib.api.client.gui.components.TextFieldWidget;
import su.plo.lib.api.client.render.MinecraftMatrix;
import su.plo.voice.config.entry.IntConfigEntry;

import java.util.regex.Pattern;

public final class NumberTextFieldWidget extends TextFieldWidget implements UpdatableWidget {

    private static final Pattern PATTERN = Pattern.compile("[0-9]+$");

    private final IntConfigEntry entry;

    public NumberTextFieldWidget(@NotNull MinecraftClientLib minecraft,
                                 @NotNull IntConfigEntry entry,
                                 int x,
                                 int y,
                                 int width,
                                 int height) {
        super(minecraft, x, y, width, height, TextComponent.empty());

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

    @Override
    public void renderButton(@NotNull GuiRender render, int mouseX, int mouseY, float delta) {
        this.x += 1;
        this.y += 1;
        MinecraftMatrix matrix = render.getMatrix();
        matrix.push();
        render.enableDepthTest();
        matrix.translate(0.0D, 0.0D, 0.0D);

        super.renderButton(render, mouseX, mouseY, delta);

        matrix.pop();
    }
}
