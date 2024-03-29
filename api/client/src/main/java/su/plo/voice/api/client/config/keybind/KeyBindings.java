package su.plo.voice.api.client.config.keybind;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface KeyBindings {

    Collection<KeyBinding.Key> getPressedKeys();

    Optional<KeyBinding> getKeyBinding(@NotNull String name);

    KeyBinding register(@NotNull String name, List<KeyBinding.Key> keys, @NotNull String category, boolean anyContext);

    void resetStates();

    Map<String, Collection<KeyBinding>> getCategories();
}
