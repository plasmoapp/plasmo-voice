package su.plo.voice.addon;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.PlasmoVoice;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.AddonDependency;
import su.plo.voice.api.addon.AddonLoaderScope;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

@RequiredArgsConstructor
public final class PlasmoVoiceAddon implements AddonContainer {

    private final @NotNull PlasmoVoice object;
    private final @NotNull AddonLoaderScope scope;

    @Override
    public @NotNull String getId() {
        return "plasmovoice";
    }

    @Override
    public @NotNull String getName() {
        return "PlasmoVoice";
    }

    @Override
    public @NotNull AddonLoaderScope getScope() {
        return scope;
    }

    @Override
    public @NotNull String getVersion() {
        return object.getVersion();
    }

    @Override
    public @NotNull Collection<String> getAuthors() {
        return Lists.newArrayList("Apehum");
    }

    @Override
    public @NotNull Collection<AddonDependency> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public @NotNull Class<?> getMainClass() {
        return BaseVoice.class;
    }

    @Override
    public Optional<?> getInstance() {
        return Optional.of(object);
    }
}
