package su.plo.voice.api.server.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.audio.capture.ActivationManager;

// todo: doc
public interface ServerActivationManager extends ActivationManager<ServerActivation> {

    /**
     * Creates a new activation builder
     */
    @NotNull ServerActivation.Builder createBuilder(@NotNull Object addonObject,
                                                    @NotNull String name,
                                                    @NotNull String translation,
                                                    @NotNull String icon,
                                                    @NotNull String permission,
                                                    int weight);
}
