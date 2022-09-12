package su.plo.voice.client.gui.tab;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.client.config.keybind.ConfigKeyBindings;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widget.KeyBindingWidget;

import java.util.Collection;

public final class HotKeysTabWidget extends KeyBindingTabWidget {

    @Getter
    private KeyBindingWidget focusedBinding;

    public HotKeysTabWidget(Minecraft client, VoiceSettingsScreen parent, KeyBindings keyBindings) {
        super(client, parent, keyBindings);
    }

    public void setFocusedKeyBinding(KeyBindingWidget focusedBinding) {
        this.focusedBinding = focusedBinding;
        keyBindings.resetStates();
    }

    @Override
    public void init() {
        // todo: keybindings
        ((ConfigKeyBindings) keyBindings)
                .getCategoryEntries()
                .asMap()
                .forEach(this::createCategory);
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

    private void createCategory(String category, Collection<KeyBindingConfigEntry> list) {
        if (category.equals("hidden")) return;

        addEntry(new CategoryEntry(Component.translatable(category)));

        list.forEach(entry ->
                addEntry(createKeyBinding(
                        entry.value().getName(),
                        null,
                        entry
                ))
        );
    }
}
