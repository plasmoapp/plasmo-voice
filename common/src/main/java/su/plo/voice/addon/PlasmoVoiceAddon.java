package su.plo.voice.addon;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonContainer;

import java.nio.file.Path;
import java.util.Optional;

@RequiredArgsConstructor
public class PlasmoVoiceAddon implements AddonContainer {

    private final @NotNull PlasmoVoice object;

    @Override
    public String getId() {
        return "plasmovoice";
    }

    @Override
    public Class<?> getMainClass() {
        return BaseVoice.class;
    }

    @Override
    public Path getPath() {
        return null;
    }

    @Override
    public Optional<?> getInstance() {
        return Optional.of(object);
    }
}
