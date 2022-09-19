package su.plo.lib.client.locale;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.chat.TextComponent;

public interface MinecraftLanguage {

    @NotNull String getOrDefault(@NotNull String key);

    boolean has(@NotNull String key);

    @NotNull TextComponent getKeyDisplayName(@NotNull KeyBinding.Key key);
}
