package su.plo.voice.client.config.keybind;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import su.plo.config.Config;
import su.plo.config.ConfigField;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.event.key.KeyPressedEvent;

import java.util.*;

@Config
public final class ConfigKeyBindings implements KeyBindings {

    @Getter
    @Setter
    @ConfigField
    private Map<String, KeyBindingConfigEntry> keyBindings = Maps.newHashMap();

    private final ListMultimap<String, KeyBindingConfigEntry> categoryEntries = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

    @Getter
    private final Set<KeyBinding.Key> pressedKeys = new HashSet<>();

    @Override
    public synchronized Optional<KeyBinding> getKeyBinding(@NotNull String name) {
        if (keyBindings.containsKey(name)) return Optional.empty();
        return Optional.of(keyBindings.get(name).value());
    }

    @Override
    public synchronized void register(@NotNull String name, List<KeyBinding.Key> keys, @NotNull String category, boolean anyContext) {
        KeyBinding keyBinding = new VoiceKeyBinding(this, name, keys);

        if (categoryEntries.containsKey(category)) {
            boolean keyBindingExists = categoryEntries.get(category)
                    .stream()
                    .anyMatch(entry -> entry.value().equals(keyBinding));
            if (keyBindingExists) {
                throw new IllegalArgumentException("KeyBinding with the same name in this category is already exists");
            }
        }

        KeyBindingConfigEntry entry = new KeyBindingConfigEntry(keyBinding);

        categoryEntries.put(category, entry);
        keyBindings.put(name, entry);
    }

    @Override
    public void resetStates() {
        keyBindings.values().forEach(entry -> entry.value().resetState());
    }

    @Override
    public Collection<String> getCategories() {
        return categoryEntries.keys();
    }

    @EventSubscribe
    public void onKeyPressed(KeyPressedEvent event) {
        if (event.getAction() == KeyBinding.Action.UP) {
            pressedKeys.remove(event.getKey());
        } else {
            pressedKeys.add(event.getKey());
        }

        keyBindings.values().forEach(entry -> entry.value().updateState(event.getAction()));
    }
}
