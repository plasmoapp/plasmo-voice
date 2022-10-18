package su.plo.lib.api.client.locale;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.voice.api.client.config.keybind.KeyBinding;

public interface MinecraftLanguage {

    @NotNull String getOrDefault(@NotNull String key);

    boolean has(@NotNull String key);

    @NotNull MinecraftTextComponent getKeyDisplayName(@NotNull KeyBinding.Key key);
}
