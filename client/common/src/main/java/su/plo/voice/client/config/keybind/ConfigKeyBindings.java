package su.plo.voice.client.config.keybind;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.SerializableConfigEntry;
import su.plo.voice.api.client.config.keybind.KeyBinding;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.event.key.KeyPressedEvent;

import java.util.*;
import java.util.stream.Collectors;

@ToString
public final class ConfigKeyBindings implements KeyBindings, SerializableConfigEntry {

    @Getter
    @Setter
    private Map<String, KeyBindingConfigEntry> keyBindings = Maps.newHashMap();

    @Getter
    private final ListMultimap<String, KeyBindingConfigEntry> categoryEntries = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

    @Getter
    private final Set<KeyBinding.Key> pressedKeys = new HashSet<>();

    public ConfigKeyBindings() {
        // proximity
        register(
                "key.plasmovoice.proximity.ptt",
                ImmutableList.of(KeyBinding.Type.KEYSYM.getOrCreate(342)), // GLFW_KEY_LEFT_ALT
                "hidden",
                true
        );

        // general
        register(
                "key.plasmovoice.general.mute_microphone",
                ImmutableList.of(KeyBinding.Type.KEYSYM.getOrCreate(77)), // GLFW_KEY_M
                "key.plasmovoice.general",
                false
        );
        register(
                "key.plasmovoice.general.disable_voice",
                ImmutableList.of(),
                "key.plasmovoice.general",
                false
        );
        register(
                "key.plasmovoice.general.action",
                ImmutableList.of(KeyBinding.Type.MOUSE.getOrCreate(1)), // GLFW_MOUSE_BUTTON_2
                "key.plasmovoice.general",
                false
        );

        // occlusion
        register(
                "key.plasmovoice.occlusion.toggle",
                ImmutableList.of(),
                "key.plasmovoice.occlusion",
                false
        );
    }

    @Override
    public synchronized Optional<KeyBinding> getKeyBinding(@NotNull String name) {
        if (!keyBindings.containsKey(name)) return Optional.empty();
        return Optional.of(keyBindings.get(name).value());
    }

    public synchronized Optional<KeyBindingConfigEntry> getConfigKeyBinding(@NotNull String name) {
        return Optional.ofNullable(keyBindings.get(name));
    }

    @Override
    public synchronized KeyBinding register(@NotNull String name, List<KeyBinding.Key> keys, @NotNull String category, boolean anyContext) {
        KeyBinding keyBinding = new VoiceKeyBinding(this, name, keys, anyContext);

        if (categoryEntries.containsKey(category)) {
            boolean keyBindingExists = categoryEntries
                    .values()
                    .stream()
                    .anyMatch(entry -> entry.value().equals(keyBinding));
            if (keyBindingExists) {
                throw new IllegalArgumentException("KeyBinding with the same name is already exists");
            }
        }

        KeyBindingConfigEntry entry = new KeyBindingConfigEntry(keyBinding);

        categoryEntries.put(category, entry);
        keyBindings.put(name, entry);

        return keyBinding;
    }

    @Override
    public synchronized void resetStates() {
        keyBindings.values().forEach(entry -> entry.value().resetState());
    }

    @Override
    public Map<String, Collection<KeyBinding>> getCategories() {
        Map<String, Collection<KeyBinding>> categories = new HashMap<>();

        categoryEntries.asMap().forEach((category, list) -> {
            categories.put(
                    category,
                    list.stream()
                            .map(ConfigEntry::value)
                            .collect(Collectors.toList())
            );
        });

        return categories;
    }

    @Override
    public synchronized void deserialize(Object object) {
        try {
            List<Object> serialized = (List<Object>) object;
            serialized.forEach((value) -> {
                Map<String, Object> configKeyMap = (Map<String, Object>) value;

                String name = (String) configKeyMap.get("name");
                String category = (String) configKeyMap.get("category");
                List<Object> configKeys = (List<Object>) configKeyMap.get("keys");
                boolean anyContext = (Boolean) configKeyMap.get("any_context");

                List<KeyBinding.Key> keys = new ArrayList<>();
                configKeys.forEach(serializedKey -> {
                    Map<String, Object> configKey = (Map<String, Object>) serializedKey;
                    KeyBinding.Type keyType = KeyBinding.Type.valueOf((String) configKey.get("type"));
                    int keyCode = ((Long) configKey.get("code")).intValue();

                    keys.add(new KeyBinding.Key(keyType, keyCode));
                });

                if (!keyBindings.containsKey(name)) {
                    register(name, ImmutableList.of(), category, anyContext);
                }

                Optional<KeyBindingConfigEntry> keybindingEntry = categoryEntries.values()
                        .stream()
                        .filter(entry -> entry.value().getName().equals(name))
                        .findFirst();

                keybindingEntry.ifPresent(keyBindingConfigEntry ->
                        keyBindingConfigEntry.set(new VoiceKeyBinding(this, name, keys, anyContext))
                );
            });
        } catch (ClassCastException ignored) {
        }
    }

    @Override
    public synchronized Object serialize() {
        List<Object> serialized = new ArrayList<>();

        categoryEntries.asMap().forEach((category, keyBindings) -> {
            keyBindings.forEach((entry) -> {
                if (entry.isDefault()) return;

                KeyBinding keyBinding = entry.value();

                Map<String, Object> serializedKeyBinding = Maps.newHashMap();

                serializedKeyBinding.put("name", keyBinding.getName());
                serializedKeyBinding.put("category", category);
                serializedKeyBinding.put(
                        "keys",
                        keyBinding.getKeys()
                                .stream()
                                .map((key) -> {
                                    Map<String, Object> serializedKey = Maps.newHashMap();
                                    serializedKey.put("type", key.getType().name());
                                    serializedKey.put("code", key.getCode());

                                    return serializedKey;
                                })
                                .collect(Collectors.toList())
                );
                serializedKeyBinding.put("any_context", keyBinding.isAnyContext());

                serialized.add(serializedKeyBinding);
            });
        });

        return serialized;
    }

    @EventSubscribe
    public void onKeyPressed(@NotNull KeyPressedEvent event) {
        if (event.getAction() == KeyBinding.Action.UP) {
            pressedKeys.remove(event.getKey());
        } else {
            pressedKeys.add(event.getKey());
        }

        keyBindings.values().forEach(entry -> {
            if (entry.value().isAnyContext() || !event.getMinecraft().getScreen().isPresent()) {
                entry.value().updateState(event.getAction());
            }
        });
    }
}
