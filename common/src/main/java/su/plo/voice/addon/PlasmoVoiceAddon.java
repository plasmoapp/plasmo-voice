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
    public String getId() {
        return "plasmovoice";
    }

    @Override
    public String getName() {
        return "PlasmoVoice";
    }

    @Override
    public @NotNull AddonLoaderScope getScope() {
        return scope;
    }

    @Override
    public String getVersion() {
        return object.getVersion();
    }

    @Override
    public Collection<String> getAuthors() {
        return Lists.newArrayList("Apehum");
    }

    @Override
    public Collection<AddonDependency> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public Class<?> getMainClass() {
        return BaseVoice.class;
    }

    @Override
    public Optional<?> getInstance() {
        return Optional.of(object);
    }
}
