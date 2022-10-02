package su.plo.lib.client.locale;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.chat.TextComponent;
import su.plo.voice.api.client.config.keybind.KeyBinding;

public interface MinecraftLanguage {

    @NotNull String getOrDefault(@NotNull String key);

    boolean has(@NotNull String key);

    @NotNull TextComponent getKeyDisplayName(@NotNull KeyBinding.Key key);
}
