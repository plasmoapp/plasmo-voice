package su.plo.voice.api.client.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.capture.ActivationManager;
import su.plo.voice.proto.data.audio.capture.Activation;

import java.util.Collection;
import java.util.Optional;

public interface ClientActivationManager extends ActivationManager<ClientActivation> {

    Optional<ClientActivation> getParentActivation();

    @NotNull ClientActivation register(@NotNull ClientActivation activation);

    @NotNull Collection<ClientActivation> register(@NotNull Collection<Activation> activations);
}
