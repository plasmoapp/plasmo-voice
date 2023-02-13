package su.plo.voice.client.gui.settings.widget;

import com.google.common.collect.ImmutableList;
import gg.essential.universal.UGraphics;
import gg.essential.universal.UKeyboard;
import gg.essential.universal.UMatrixStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.MathLib;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextStyle;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.language.LanguageUtil;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.client.gui.settings.tab.AbstractHotKeysTabWidget;

import java.util.*;
import java.util.stream.Collectors;

public final class HotKeyWidget extends Button implements UpdatableWidget {

    private final AbstractHotKeysTabWidget parent;
    private final KeyBindingConfigEntry entry;
    private final List<KeyBinding.Key> pressedKeys = new ArrayList<>();

    public HotKeyWidget(@NotNull AbstractHotKeysTabWidget parent,
                        @NotNull KeyBindingConfigEntry entry,
                        int x,
                        int y,
                        int width,
                        int height) {
        super(x, y, width, height, MinecraftTextComponent.empty(), NO_ACTION, NO_TOOLTIP);

        this.parent = parent;
        this.entry = entry;

        updateValue();
    }

    @Override
    public void updateValue() {
        MinecraftTextComponent text = MinecraftTextComponent.literal("");
        if (entry.value().getKeys().size() == 0) {
            text.append(MinecraftTextComponent.translatable("gui.none"));
        } else {
            formatKeys(text, entry.value().getKeys());
        }

        if (isActive()) {
            if (pressedKeys.size() > 0) {
                text = MinecraftTextComponent.literal("");
                List<KeyBinding.Key> sorted = pressedKeys.stream()
                        .sorted(Comparator.comparingInt(key -> key.getType().ordinal()))
                        .collect(Collectors.toList());

                formatKeys(text, sorted);
            }

            setText(
                    MinecraftTextComponent.literal("> ").withStyle(MinecraftTextStyle.YELLOW)
                            .append(text.withStyle(MinecraftTextStyle.YELLOW))
                            .append(MinecraftTextComponent.literal(" <").withStyle(MinecraftTextStyle.YELLOW))
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
    public boolean keyPressed(int keyCode, char typedChar, @Nullable UKeyboard.Modifiers modifiers) {
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

        return super.keyPressed(keyCode, typedChar, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, char typedChar, @Nullable UKeyboard.Modifiers modifiers) {
        if (isActive()
                && pressedKeys.stream().anyMatch(key -> key.getType() == KeyBinding.Type.KEYSYM && key.getCode() == keyCode)
        ) {
            keysReleased();
            updateValue();
            return true;
        }

        return super.keyReleased(keyCode, typedChar, modifiers);
    }

    @Override
    protected void renderText(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        int j = active ? 16777215 : 10526880;

        if (Objects.equals(parent.getFocusedHotKey(), this)) {
            RenderUtil.drawCenteredString(
                    stack,
                    getText(),
                    x + width / 2,
                    y + height / 2 - UGraphics.getFontHeight() / 2,
                    j | MathLib.ceil(alpha * 255.0F) << 24
            );
        } else {
            RenderUtil.drawCenteredOrderedString(
                    stack,
                    getText(),
                    width - 16,
                    x + width / 2,
                    y + height / 2 - UGraphics.getFontHeight() / 2,
                    j | MathLib.ceil(alpha * 255.0F) << 24
            );
        }
    }

    @Override
    public void renderToolTip(@NotNull UMatrixStack stack, int mouseX, int mouseY) {
        if (!Objects.equals(parent.getFocusedHotKey(), this)) {
            int width = RenderUtil.getTextWidth(getText());
            if (width > this.width - 16) {
                parent.setTooltip(ImmutableList.of(getText()));
            }
        }

        super.renderToolTip(stack, mouseX, mouseY);
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

    private void formatKeys(MinecraftTextComponent text, Collection<KeyBinding.Key> keys) {
        for (KeyBinding.Key key : keys) {
            text.append(LanguageUtil.getKeyDisplayName(key));
            text.append(MinecraftTextComponent.literal(" + "));
        }

        text.siblings().remove(text.siblings().size() - 1);
    }
}
