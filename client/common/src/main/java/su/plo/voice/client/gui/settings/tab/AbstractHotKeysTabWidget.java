package su.plo.voice.client.gui.settings.tab;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.lib.client.gui.GuiUtil;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.chat.TextComponent;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.gui.settings.widget.HotKeyWidget;

public abstract class AbstractHotKeysTabWidget extends TabWidget {

    protected final KeyBindings hotKeys;
    @Getter
    private HotKeyWidget focusedHotKey;

    public AbstractHotKeysTabWidget(@NotNull MinecraftClientLib minecraft,
                                    @NotNull VoiceSettingsScreen parent,
                                    @NotNull PlasmoVoiceClient voiceClient,
                                    @NotNull ClientConfig config) {
        super(minecraft, parent, voiceClient, config);

        this.hotKeys = voiceClient.getKeyBindings();
    }

    @Override
    public void removed() {
        super.removed();

        this.focusedHotKey = null;
        updateOptionEntries((widget) -> widget instanceof HotKeyWidget);

    }

    public void setFocusedHotKey(@Nullable HotKeyWidget focusedBinding) {
        this.focusedHotKey = focusedBinding;
        hotKeys.resetStates();
    }

    protected OptionEntry<HotKeyWidget> createHotKey(@NotNull String translatable,
                                                             @Nullable String tooltipTranslatable,
                                                             @NotNull KeyBindingConfigEntry entry) {
        HotKeyWidget keyBinding = new HotKeyWidget(
                minecraft,
                this,
                entry,
                0,
                0,
                ELEMENT_WIDTH,
                20
        );

        return new OptionEntry<>(
                TextComponent.translatable(translatable),
                keyBinding,
                entry,
                GuiUtil.multiLineTooltip(minecraft.getLanguage(), tooltipTranslatable)
        );
    }
}
