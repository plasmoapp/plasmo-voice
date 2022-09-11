package su.plo.voice.client.gui.tab;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.widget.KeyBindingWidget;

public final class KeyBindingsTabWidget extends TabWidget {

    private final KeyBindings keyBindings;

    @Getter
    private KeyBindingWidget focusedBinding;

    public KeyBindingsTabWidget(Minecraft client, VoiceSettingsScreen parent, KeyBindings keyBindings) {
        super(client, parent);

        this.keyBindings = keyBindings;
    }

    public void setFocusedKeyBinding(KeyBindingWidget focusedBinding) {
        this.focusedBinding = focusedBinding;
        keyBindings.resetStates();
    }

    @Override
    public void init() {
        // todo: keybindings
//        ClientConfig config = VoiceClient.getClientConfig();
//
//        for (String category : config.keyBindings.categories) {
//            List<ClientConfig.KeyBindingConfigEntry> keyBindings = config.keyBindings.categoryEntries.get(category);
//            this.addEntry(new CategoryEntry(Component.translatable(category)));
//            for (ClientConfig.KeyBindingConfigEntry keyBinding : keyBindings) {
//                this.addEntry(new OptionEntry(
//                        keyBinding.get().getTranslation(),
//                        new KeyBindWidget(this, 0, 0, 97, 20, keyBinding),
//                        keyBinding,
//                        (button, element) -> {
//                            ((KeyBindWidget) element).updateValue();
//                        })
//                );
//            }
//        }
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
}
