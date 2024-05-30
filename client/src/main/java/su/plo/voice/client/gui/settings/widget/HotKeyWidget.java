package su.plo.voice.client.gui.settings.widget;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import su.plo.slib.api.chat.component.McTextComponent;
import su.plo.slib.api.chat.style.McTextStyle;
import org.jetbrains.annotations.NotNull;
import su.plo.lib.mod.client.gui.components.Button;
import su.plo.lib.mod.client.language.LanguageUtil;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.client.config.hotkey.HotkeyConfigEntry;
import su.plo.voice.client.gui.settings.tab.AbstractHotKeysTabWidget;

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

        if (isActive()) {
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
        if (isActive()
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
        if (isActive()) {
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
        if (isActive()
                && pressedKeys.stream().anyMatch(key -> key.getType() == Hotkey.Type.KEYSYM && key.getCode() == keyCode)
        ) {
            keysReleased();
            updateValue();
            return true;
        }

        return super.keyReleased(keyCode, typedChar, modifiers);
    }

    @Override
    protected void renderText(@NotNull PoseStack stack, int mouseX, int mouseY) {
        int j = active ? 16777215 : 10526880;

        if (Objects.equals(parent.getFocusedHotKey(), this)) {
            RenderUtil.drawCenteredString(
                    stack,
                    getText(),
                    x + width / 2,
                    y + height / 2 - RenderUtil.getFontHeight() / 2,
                    j | Mth.ceil(alpha * 255.0F) << 24
            );
        } else {
            RenderUtil.drawCenteredOrderedString(
                    stack,
                    getText(),
                    width - 16,
                    x + width / 2,
                    y + height / 2 - RenderUtil.getFontHeight() / 2,
                    j | Mth.ceil(alpha * 255.0F) << 24
            );
        }
    }

    @Override
    public void renderToolTip(@NotNull PoseStack stack, int mouseX, int mouseY) {
        if (!Objects.equals(parent.getFocusedHotKey(), this)) {
            int width = RenderUtil.getTextWidth(getText());
            if (width > this.width - 16) {
                parent.setTooltip(getText());
            }
        }

        super.renderToolTip(stack, mouseX, mouseY);
    }

    @Override
    public boolean isActive() {
        return Objects.equals(parent.getFocusedHotKey(), this);
    }

    public void keysReleased() {
        entry.value().setKeys(ImmutableSet.copyOf(pressedKeys));
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
