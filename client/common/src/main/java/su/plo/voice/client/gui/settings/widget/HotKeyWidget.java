package su.plo.voice.client.gui.settings.widget;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.client.MathLib;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiRender;
import su.plo.lib.client.gui.components.Button;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.chat.TextStyle;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.client.gui.settings.tab.AbstractHotKeysTabWidget;

import java.util.*;
import java.util.stream.Collectors;

public final class HotKeyWidget extends Button implements UpdatableWidget {

    private final AbstractHotKeysTabWidget parent;
    private final KeyBindingConfigEntry entry;
    private final List<KeyBinding.Key> pressedKeys = new ArrayList<>();

    public HotKeyWidget(@NotNull MinecraftClientLib minecraft,
                        @NotNull AbstractHotKeysTabWidget parent,
                        @NotNull KeyBindingConfigEntry entry,
                        int x,
                        int y,
                        int width,
                        int height) {
        super(minecraft, x, y, width, height, TextComponent.empty(), NO_ACTION, NO_TOOLTIP);

        this.parent = parent;
        this.entry = entry;

        updateValue();
    }

    @Override
    public void updateValue() {
        TextComponent text = TextComponent.literal("");
        if (entry.value().getKeys().size() == 0) {
            text.append(TextComponent.translatable("gui.none"));
        } else {
            formatKeys(text, entry.value().getKeys());
        }

        if (isActive()) {
            if (pressedKeys.size() > 0) {
                text = TextComponent.literal("");
                List<KeyBinding.Key> sorted = pressedKeys.stream()
                        .sorted(Comparator.comparingInt(key -> key.getType().ordinal()))
                        .collect(Collectors.toList());

                formatKeys(text, sorted);
            }

            setText(
                    TextComponent.literal("> ").withStyle(TextStyle.YELLOW)
                            .append(text.withStyle(TextStyle.YELLOW))
                            .append(TextComponent.literal(" <").withStyle(TextStyle.YELLOW))
            );
        } else {
            setText(text);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isActive()
                && !(button == 0 && pressedKeys.size() == 0) // GLFW_MOUSE_BUTTON_1
                && pressedKeys.stream().anyMatch(key -> key.getType() == KeyBinding.Type.MOUSE && key.getCode() == button)
        ) {
            keysReleased();
            updateValue();
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isActive()) {
            if (pressedKeys.size() < 3) {
                pressedKeys.add(KeyBinding.Type.MOUSE.getOrCreate(button));
            }
            updateValue();
            return true;
        } else if (isClicked(mouseX, mouseY) && isValidClickButton(button)) {
            parent.setFocusedHotKey(this);
            updateValue();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isActive()) {
            if (keyCode == 256) { // GLFW_KEY_ESCAPE
                if (pressedKeys.size() > 0) {
                    keysReleased();
                } else {
                    parent.setFocusedHotKey(null);
                    entry.value().getKeys().clear();
                    updateValue();
                }
                return true;
            }

            KeyBinding.Key key = KeyBinding.Type.KEYSYM.getOrCreate(keyCode);
            if (pressedKeys.size() < 3 && !pressedKeys.contains(key)) {
                pressedKeys.add(key);
            }
            updateValue();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (isActive()
                && pressedKeys.stream().anyMatch(key -> key.getType() == KeyBinding.Type.KEYSYM && key.getCode() == keyCode)
        ) {
            keysReleased();
            updateValue();
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderText(@NotNull GuiRender render, int mouseX, int mouseY) {
        int j = active ? 16777215 : 10526880;

        if (Objects.equals(parent.getFocusedHotKey(), this)) {
            render.drawCenteredString(
                    getText(),
                    x + width / 2,
                    y + height / 2 - minecraft.getFont().getLineHeight() / 2,
                    j | MathLib.ceil(alpha * 255.0F) << 24
            );
        } else {
            render.drawCenteredOrderedString(
                    getText(),
                    width - 16,
                    x + width / 2,
                    y + height / 2 - minecraft.getFont().getLineHeight() / 2,
                    j | MathLib.ceil(alpha * 255.0F) << 24
            );
        }
    }

    @Override
    public void renderToolTip(@NotNull GuiRender render, int mouseX, int mouseY) {
        if (!Objects.equals(parent.getFocusedHotKey(), this)) {
            int width = minecraft.getFont().width(getText());
            if (width > this.width - 16) {
                parent.setTooltip(ImmutableList.of(getText()));
            }
        }

        super.renderToolTip(render, mouseX, mouseY);
    }

    @Override
    public boolean isActive() {
        return Objects.equals(parent.getFocusedHotKey(), this);
    }

    public void keysReleased() {
        entry.value().getKeys().clear();
        entry.value().getKeys().addAll(ImmutableList.copyOf(pressedKeys));
        pressedKeys.clear();
        parent.setFocusedHotKey(null);
    }

    private void formatKeys(TextComponent text, Collection<KeyBinding.Key> keys) {
        for (KeyBinding.Key key : keys) {
            text.append(minecraft.getLanguage().getKeyDisplayName(key));
            text.append(TextComponent.literal(" + "));
        }

        text.getSiblings().remove(text.getSiblings().size() - 1);
    }
}
