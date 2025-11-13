package su.plo.voice.client.gui.settings.widget;

import com.google.common.collect.ImmutableSet;
import su.plo.lib.mod.client.render.Colors;
import su.plo.lib.mod.client.render.gui.GuiRenderContext;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextStyle;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.language.LanguageUtil;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.client.config.hotkey.HotkeyConfigEntry;
import su.plo.voice.client.gui.settings.tab.AbstractHotKeysTabWidget;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collectors;

public final class HotKeyWidget extends Button implements UpdatableWidget {

    private final AbstractHotKeysTabWidget parent;
    private final HotkeyConfigEntry entry;
    private final List<Hotkey.Key> pressedKeys = new ArrayList<>();

    public HotKeyWidget(@NotNull AbstractHotKeysTabWidget parent,
                        @NotNull HotkeyConfigEntry entry,
                        int x,
                        int y,
                        int width,
                        int height) {
        super(x, y, width, height, McTextComponent.empty(), NO_ACTION, NO_TOOLTIP);

        this.parent = parent;
        this.entry = entry;

        updateValue();
    }

    @Override
    public void updateValue() {
        McTextComponent text = McTextComponent.literal("");
        if (entry.value().getKeys().size() == 0) {
            text.append(McTextComponent.translatable("gui.none"));
        } else {
            formatKeys(text, entry.value().getKeys());
        }

        if (isActiveHotkey()) {
            if (pressedKeys.size() > 0) {
                text = McTextComponent.literal("");
                List<Hotkey.Key> sorted = pressedKeys.stream()
                        .sorted(Comparator.comparingInt(key -> key.getType().ordinal()))
                        .collect(Collectors.toList());

                formatKeys(text, sorted);
            }

            setText(
                    McTextComponent.literal("> ").withStyle(McTextStyle.YELLOW)
                            .append(text.withStyle(McTextStyle.YELLOW))
                            .append(McTextComponent.literal(" <").withStyle(McTextStyle.YELLOW))
            );
        } else {
            setText(text);
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isActiveHotkey()
                && !(button == 0 && pressedKeys.size() == 0) // GLFW_MOUSE_BUTTON_1
                && pressedKeys.stream().anyMatch(key -> key.getType() == Hotkey.Type.MOUSE && key.getCode() == button)
        ) {
            keysReleased();
            updateValue();
            return true;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isActiveHotkey()) {
            if (pressedKeys.size() < 3) {
                pressedKeys.add(Hotkey.Type.MOUSE.getOrCreate(button));
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
    public boolean keyPressed(int keyCode, int modifiers) {
        if (isActiveHotkey()) {
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

            Hotkey.Key key = Hotkey.Type.KEYSYM.getOrCreate(keyCode);
            if (pressedKeys.size() < 3 && !pressedKeys.contains(key)) {
                pressedKeys.add(key);
            }
            updateValue();
            return true;
        }

        return super.keyPressed(keyCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, char typedChar, int modifiers) {
        if (isActiveHotkey()
                && pressedKeys.stream().anyMatch(key -> key.getType() == Hotkey.Type.KEYSYM && key.getCode() == keyCode)
        ) {
            keysReleased();
            updateValue();
            return true;
        }

        return super.keyReleased(keyCode, typedChar, modifiers);
    }

    @Override
    protected void renderText(@NotNull GuiRenderContext context, int mouseX, int mouseY) {
        Color textColor = Colors.withAlpha(active ? Colors.WHITE : Colors.GRAY, alpha);

        if (Objects.equals(parent.getFocusedHotKey(), this)) {
            context.drawCenteredString(
                    getText(),
                    x + width / 2,
                    y + height / 2 - RenderUtil.getFontHeight() / 2,
                    textColor
            );
        } else {
            context.drawCenteredOrderedString(
                    getText(),
                    width - 16,
                    x + width / 2,
                    y + height / 2 - RenderUtil.getFontHeight() / 2,
                    textColor
            );
        }
    }

    @Override
    public void renderToolTip(@NotNull GuiRenderContext context, int mouseX, int mouseY) {
        if (!isActiveHotkey()) {
            int width = RenderUtil.getTextWidth(getText());
            if (width > this.width - 16) {
                parent.setTooltip(getText());
            }
        }

        super.renderToolTip(context, mouseX, mouseY);
    }

    public boolean isActiveHotkey() {
        return Objects.equals(parent.getFocusedHotKey(), this);
    }

    public void keysReleased() {
        entry.updateKeys(ImmutableSet.copyOf(pressedKeys));
        pressedKeys.clear();
        parent.setFocusedHotKey(null);
    }

    private void formatKeys(McTextComponent text, Collection<Hotkey.Key> keys) {
        for (Hotkey.Key key : keys) {
            text.append(LanguageUtil.getKeyDisplayName(key));
            text.append(McTextComponent.literal(" + "));
        }

        text.getSiblings().remove(text.getSiblings().size() - 1);
    }
}
