package su.plo.voice.client.gui.tab;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.client.gui.GuiUtil;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widget.KeyBindingWidget;

public abstract class KeyBindingTabWidget extends TabWidget {

    private final KeyBindings keyBindings;
    @Getter
    private KeyBindingWidget focusedBinding;

    public KeyBindingTabWidget(Minecraft minecraft, VoiceSettingsScreen parent, KeyBindings keyBindings) {
        super(minecraft, parent);

        this.keyBindings = keyBindings;
    }

    public void setFocusedKeyBinding(KeyBindingWidget focusedBinding) {
        this.focusedBinding = focusedBinding;
        keyBindings.resetStates();
    }

    @Override
    public void onClose() {
        this.focusedBinding = null;
        for (Entry entry : children()) {
            if (entry instanceof OptionEntry &&
                    entry.children().get(0) instanceof KeyBindingWidget keyBindWidget) {
                keyBindWidget.updateValue();
            }
        }
        super.onClose();
    }

    protected OptionEntry<KeyBindingWidget> createKeyBinding(@NotNull String translatable,
                                                          @Nullable String tooltipTranslatable,
                                                          @NotNull KeyBindingConfigEntry entry) {
        KeyBindingWidget keyBinding = new KeyBindingWidget(
                this,
                0,
                0,
                97,
                20,
                entry
        );

        return new OptionEntry<>(
                Component.translatable(translatable),
                keyBinding,
                entry,
                GuiUtil.multiLineTooltip(tooltipTranslatable)
        );
    }
}
