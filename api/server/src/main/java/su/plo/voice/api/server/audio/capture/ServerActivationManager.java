package su.plo.voice.api.server.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.capture.ActivationManager;

import java.util.List;
import java.util.Optional;

// todo: doc
public interface ServerActivationManager extends ActivationManager<ServerActivation> {

    Optional<ServerActivation> register(@NotNull Object addonObject,
                                        @NotNull String name,
                                        @NotNull String translation,
                                        @NotNull String icon,
                                        List<Integer> distances,
                                        int defaultDistance,
                                        boolean transitive,
                                        boolean stereoSupported,
                                        int weight);
}
