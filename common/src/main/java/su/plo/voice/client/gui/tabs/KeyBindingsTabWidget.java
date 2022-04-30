package su.plo.voice.client.gui.tabs;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widgets.KeyBindWidget;

import java.util.List;

public class KeyBindingsTabWidget extends TabWidget {
    @Getter
    private KeyBindWidget focusedBinding;

    public KeyBindingsTabWidget(Minecraft client, VoiceSettingsScreen parent) {
        super(client, parent);

        ClientConfig config = VoiceClient.getClientConfig();

        for (String category : config.keyBindings.categories) {
            List<ClientConfig.KeyBindingConfigEntry> keyBindings = config.keyBindings.categoryEntries.get(category);
            this.addEntry(new CategoryEntry(this, new TranslatableComponent(category)));
            for (ClientConfig.KeyBindingConfigEntry keyBinding : keyBindings) {
                this.addEntry(new OptionEntry(
                        this,
                        keyBinding.get().getTranslation(),
                        new KeyBindWidget(this, 0, 0, 97, 20, keyBinding),
                        keyBinding,
                        (button, element) -> {
                            ((KeyBindWidget) element).updateValue();
                        })
                );
            }
        }
    }

    public void setFocusedBinding(KeyBindWidget focusedBinding) {
        this.focusedBinding = focusedBinding;
        VoiceClient.getKeyBindings().resetKeys();
    }

    @Override
    public void onClose() {
        this.focusedBinding = null;
        for (Entry entry : children()) {
            if (entry instanceof OptionEntry &&
                    entry.children().get(0) instanceof KeyBindWidget) {
                KeyBindWidget keyBindWidget = (KeyBindWidget) entry.children().get(0);
                keyBindWidget.updateValue();
            }
        }
        super.onClose();
    }
}
