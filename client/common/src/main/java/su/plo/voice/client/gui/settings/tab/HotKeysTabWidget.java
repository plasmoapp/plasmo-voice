package su.plo.voice.client.gui.settings.tab;

import su.plo.lib.chat.TextComponent;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.ClientActivation;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.client.audio.capture.VoiceClientActivation;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.keybind.ConfigKeyBindings;
import su.plo.voice.client.config.keybind.KeyBindingConfigEntry;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;

import java.util.Collection;

public final class HotKeysTabWidget extends AbstractHotKeysTabWidget {

    private final ClientActivationManager activations;

    public HotKeysTabWidget(MinecraftClientLib minecraft,
                            VoiceSettingsScreen parent,
                            PlasmoVoiceClient voiceClient,
                            ClientConfig config) {
        super(minecraft, parent, voiceClient, config);

        this.activations = voiceClient.getActivationManager();
    }

    @Override
    public void init() {
        super.init();

        ((ConfigKeyBindings) hotKeys)
                .getCategoryEntries()
                .asMap()
                .forEach(this::createCategory);

        activations.getParentActivation()
                .ifPresent(this::createActivation);
        activations.getActivations()
                .forEach(this::createActivation);
    }

    private void createActivation(ClientActivation activation) {
        if (activation.getDistances().size() == 0) return;

        addEntry(new CategoryEntry(TextComponent.translatable("key.plasmovoice.distance", TextComponent.translatable(activation.getTranslation()))));

        VoiceClientActivation clientActivation = (VoiceClientActivation) activation;

        addEntry(createHotKey(
                "key.plasmovoice.distance.increase",
                null,
                clientActivation.getDistanceIncreaseConfigEntry()
        ));
        addEntry(createHotKey(
                "key.plasmovoice.distance.decrease",
                null,
                clientActivation.getDistanceDecreaseConfigEntry()
        ));
    }

    private void createCategory(String category, Collection<KeyBindingConfigEntry> list) {
        if (category.equals("hidden")) return;

        addEntry(new CategoryEntry(TextComponent.translatable(category)));

        list.forEach(entry ->
                addEntry(createHotKey(
                        entry.value().getName(),
                        null,
                        entry
                ))
        );
    }
}
