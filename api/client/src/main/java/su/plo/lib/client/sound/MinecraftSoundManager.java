package su.plo.lib.client.sound;

import org.jetbrains.annotations.NotNull;

public interface MinecraftSoundManager {

    void playSound(@NotNull Category category, @NotNull String soundId, float pitch);

    void playSound(@NotNull Category category, @NotNull String soundId, float pitch, float volume);

    enum Category {

        UI
    }
}
