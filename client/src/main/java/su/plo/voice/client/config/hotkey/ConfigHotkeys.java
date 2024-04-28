package su.plo.voice.client.config.hotkey;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import gg.essential.universal.UScreen;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.config.entry.ConfigEntry;
import su.plo.config.entry.SerializableConfigEntry;
import su.plo.voice.api.client.config.hotkey.Hotkey;
import su.plo.voice.api.client.config.hotkey.Hotkeys;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.event.key.KeyPressedEvent;

import java.util.*;
import java.util.stream.Collectors;

@ToString
public final class ConfigHotkeys implements Hotkeys, SerializableConfigEntry {

    @Getter
    @Setter
    private Map<String, HotkeyConfigEntry> hotkeys = Maps.newHashMap();

    @Getter
    private final ListMultimap<String, HotkeyConfigEntry> categoryEntries = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

    @Getter
    private final Set<Hotkey.Key> pressedKeys = new HashSet<>();

    public ConfigHotkeys() {
        // proximity
        register(
                "key.plasmovoice.proximity.ptt",
                ImmutableList.of(Hotkey.Type.KEYSYM.getOrCreate(342)), // GLFW_KEY_LEFT_ALT
                "hidden",
                true
        );

        // general
        register(
                "key.plasmovoice.general.mute_microphone",
                ImmutableList.of(Hotkey.Type.KEYSYM.getOrCreate(77)), // GLFW_KEY_M
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
                ImmutableList.of(Hotkey.Type.MOUSE.getOrCreate(1)), // GLFW_MOUSE_BUTTON_2
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
    public synchronized Optional<Hotkey> getHotkey(@NotNull String name) {
        if (!hotkeys.containsKey(name)) return Optional.empty();
        return Optional.of(hotkeys.get(name).value());
    }

    public synchronized Optional<HotkeyConfigEntry> getConfigHotkey(@NotNull String name) {
        return Optional.ofNullable(hotkeys.get(name));
    }

    @Override
    public synchronized @NotNull Hotkey register(@NotNull String name, List<Hotkey.Key> keys, @NotNull String category, boolean anyContext) {
        Hotkey hotkey = new VoiceHotkey(this, name, keys, anyContext);

        if (categoryEntries.containsKey(category)) {
            boolean hotkeyExists = categoryEntries
                    .values()
                    .stream()
                    .anyMatch(entry -> entry.value().equals(hotkey));
            if (hotkeyExists) {
                throw new IllegalArgumentException("Hotkey with the same name is already exists");
            }
        }

        HotkeyConfigEntry entry = new HotkeyConfigEntry(hotkey);

        categoryEntries.put(category, entry);
        hotkeys.put(name, entry);

        return hotkey;
    }

    @Override
    public synchronized void resetPressedStates() {
        hotkeys.values().forEach(entry -> entry.value().resetState());
    }

    @Override
    public @NotNull Map<String, Collection<Hotkey>> getCategories() {
        Map<String, Collection<Hotkey>> categories = new HashMap<>();

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

                List<Hotkey.Key> keys = new ArrayList<>();
                configKeys.forEach(serializedKey -> {
                    Map<String, Object> configKey = (Map<String, Object>) serializedKey;
                    Hotkey.Type keyType = Hotkey.Type.valueOf((String) configKey.get("type"));
                    int keyCode = ((Long) configKey.get("code")).intValue();

                    keys.add(new Hotkey.Key(keyType, keyCode));
                });

                if (!hotkeys.containsKey(name)) {
                    register(name, ImmutableList.of(), category, anyContext);
                }

                Optional<HotkeyConfigEntry> hotkeyEntry = categoryEntries.values()
                        .stream()
                        .filter(entry -> entry.value().getName().equals(name))
                        .findFirst();

                hotkeyEntry.ifPresent(entry ->
                        entry.set(new VoiceHotkey(this, name, keys, anyContext))
                );
            });
        } catch (ClassCastException ignored) {
        }
    }

    @Override
    public synchronized Object serialize() {
        List<Object> serialized = new ArrayList<>();

        categoryEntries.asMap().forEach((category, hotkeys) -> {
            hotkeys.forEach((entry) -> {
                if (entry.isDefault()) return;

                Hotkey hotkey = entry.value();

                Map<String, Object> serializedHotkey = Maps.newHashMap();

                serializedHotkey.put("name", hotkey.getName());
                serializedHotkey.put("category", category);
                serializedHotkey.put(
                        "keys",
                        hotkey.getKeys()
                                .stream()
                                .map((key) -> {
                                    Map<String, Object> serializedKey = Maps.newHashMap();
                                    serializedKey.put("type", key.getType().name());
                                    serializedKey.put("code", key.getCode());

                                    return serializedKey;
                                })
                                .collect(Collectors.toList())
                );
                serializedHotkey.put("any_context", hotkey.isAnyContext());

                serialized.add(serializedHotkey);
            });
        });

        return serialized;
    }

    @EventSubscribe
    public void onKeyPressed(@NotNull KeyPressedEvent event) {
        if (event.getAction() == Hotkey.Action.UP) {
            pressedKeys.remove(event.getKey());
        } else {
            pressedKeys.add(event.getKey());
        }

        hotkeys.values().forEach(entry -> {
            if (entry.value().isAnyContext() || UScreen.getCurrentScreen() == null) {
                entry.value().updateState(event.getAction());
            }
        });
    }
}
