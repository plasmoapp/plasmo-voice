package su.plo.lib.api.client.sound;

import org.jetbrains.annotations.NotNull;

public interface MinecraftSoundManager {

    void playSound(@NotNull Category category, @NotNull String soundLocation, float pitch);

    void playSound(@NotNull Category category, @NotNull String soundLocation, float pitch, float volume);

    enum Category {

        UI
    }
}
