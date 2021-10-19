package su.plo.voice.client.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.mojang.blaze3d.platform.InputConstants;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.config.entries.*;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.gui.tabs.KeyBindingsTabWidget;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

// govnokod
public class ClientConfig {
    private static Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(BooleanConfigEntry.class, new BooleanConfigEntry());
        builder.registerTypeAdapter(IntegerConfigEntry.class, new IntegerConfigEntry(0, 0));
        builder.registerTypeAdapter(MicrophoneIconPositionConfigEntry.class, new MicrophoneIconPositionConfigEntry());
        builder.registerTypeAdapter(DoubleConfigEntry.class, new DoubleConfigEntry(0, 0));
        builder.registerTypeAdapter(StringConfigEntry.class, new StringConfigEntry());
        builder.registerTypeAdapter(KeyBindingConfigEntry.class, new KeyBindingConfigEntry());
        gson = builder.create();
    }

    @Getter
    private final HashMap<String, ServerConfig> servers = new HashMap<>();
    @Getter
    private final HashMap<UUID, Double> playerVolumes = new HashMap<>();
    public BooleanConfigEntry whitelist = new BooleanConfigEntry();

    // audio
    public DoubleConfigEntry voiceVolume = new DoubleConfigEntry(0, 2);
    public DoubleConfigEntry priorityVolume = new DoubleConfigEntry(0, 2);
    public BooleanConfigEntry occlusion = new BooleanConfigEntry();
    public BooleanConfigEntry speakerMuted = new BooleanConfigEntry();

    // icons
    public IntegerConfigEntry showIcons = new IntegerConfigEntry(0, 2);
    public MicrophoneIconPositionConfigEntry micIconPosition = new MicrophoneIconPositionConfigEntry();

    // voice
    public BooleanConfigEntry voiceActivation = new BooleanConfigEntry();
    public DoubleConfigEntry voiceActivationThreshold = new DoubleConfigEntry(-60, 0);

    // microphone
    public DoubleConfigEntry microphoneAmplification = new DoubleConfigEntry(0, 2);
    public StringConfigEntry microphone = new StringConfigEntry();
    public StringConfigEntry speaker = new StringConfigEntry();
    public BooleanConfigEntry rnNoise = new BooleanConfigEntry();
    public BooleanConfigEntry microphoneMuted = new BooleanConfigEntry();
    public BooleanConfigEntry javaxCapture = new BooleanConfigEntry();

    // sound engine
    public BooleanConfigEntry hrtf = new BooleanConfigEntry();
    public BooleanConfigEntry directionalSources = new BooleanConfigEntry();
    public IntegerConfigEntry directionalSourcesAngle = new IntegerConfigEntry(100, 360);

    public BooleanConfigEntry compressor = new BooleanConfigEntry();
    public IntegerConfigEntry compressorThreshold = new IntegerConfigEntry(-60, 0);
    public IntegerConfigEntry limiterThreshold = new IntegerConfigEntry(-60, 0);

    // visual
    public BooleanConfigEntry visualizeDistance = new BooleanConfigEntry();

    // hotkeys
    public ConfigKeyBindings keyBindings = new ConfigKeyBindings();

    // UI
    public BooleanConfigEntry showPriorityVolume = new BooleanConfigEntry();

    @Getter
    private transient HashSet<UUID> muted = new HashSet<>();
    @Getter
    private transient HashSet<UUID> whitelisted = new HashSet<>();

    public ClientConfig() {
    }

    public static ClientConfig read() {
        ClientConfig config = null;
        File configFile = new File("config/PlasmoVoice/config.json");
        if(configFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(configFile));
                config = gson.fromJson(reader, ClientConfig.class);
            } catch (FileNotFoundException ignored) {} catch (JsonSyntaxException e) {
                configFile.delete();
            }
        }

        if(config == null) {
            config = new ClientConfig();
        }

        // config defaults
        config.setupDefaults();

        ClientData data = ClientData.read();
        if(data.mutedClients != null) {
            config.muted = data.mutedClients;
        }
        if(data.whitelisted != null) {
            config.whitelisted = data.whitelisted;
        }

        return config;
    }

    public void save() {
        // async write
        new Thread(() -> {
            File configDir = new File("config/PlasmoVoice");
            configDir.mkdirs();

            try {
                try(Writer w = new FileWriter("config/PlasmoVoice/config.json")) {
                    w.write(gson.toJson(this));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        (new ClientData(muted, whitelisted)).save();
    }

    public ServerConfig getCurrentServerConfig() {
        return servers.get(VoiceClient.getServerConfig().getIp());
    }

    protected void setupDefaults() {
        occlusion.setDefault(false);
        showIcons.setDefault(0, 0, 2);
        micIconPosition.setDefault(MicrophoneIconPosition.BOTTOM_CENTER);
        voiceActivation.setDefault(false);
        voiceActivationThreshold.setDefault(-30, -60, 0);
        voiceVolume.setDefault(1, 0, 2);
        priorityVolume.setDefault(1, 0, 2);
        microphoneAmplification.setDefault(1, 0, 2);
        whitelist.setDefault(false);
        hrtf.setDefault(false);
        microphoneMuted.setDefault(false);
        javaxCapture.setDefault(false);
        speakerMuted.setDefault(false);
        rnNoise.setDefault(false);
        directionalSources.setDefault(false);
        directionalSourcesAngle.setDefault(145, 100, 360);
        visualizeDistance.setDefault(true);
        showPriorityVolume.setDefault(true);
        compressor.setDefault(true);
        compressorThreshold.setDefault(-10, -60, 0);
        limiterThreshold.setDefault(-6, -60, 0);

        keyBindings.setupDefaults();
    }

    // mute stuff
    public boolean isMuted(UUID uuid) {
        if(whitelist.get()) {
            return !whitelisted.contains(uuid);
        } else {
            return muted.contains(uuid);
        }
    }

    public void mute(UUID uuid) {
        if(whitelist.get()) {
            whitelisted.remove(uuid);
        } else {
            muted.add(uuid);
        }
        save();
    }

    public void unmute(UUID uuid) {
        if(whitelist.get()) {
            whitelisted.add(uuid);
        } else {
            muted.remove(uuid);
        }
        save();
    }

    public double getPlayerVolume(UUID uuid, boolean priority) {
        DoubleConfigEntry entry;
        if (showPriorityVolume.get() && priority) {
            entry = priorityVolume;
        } else {
            entry = voiceVolume;
        }

        return entry.get() * playerVolumes.getOrDefault(uuid, 1.0D);
    }

    public static class ServerConfig {
        public IntegerConfigEntry distance = new IntegerConfigEntry(0, Short.MAX_VALUE);
        public IntegerConfigEntry priorityDistance = new IntegerConfigEntry(0, Short.MAX_VALUE);
    }

    public static class ConfigKeyBindings {
        private static final Minecraft client = Minecraft.getInstance();
        public transient List<String> categories = new ArrayList<>();
        public transient Map<String, List<KeyBindingConfigEntry>> categoryEntries = new HashMap<>();
        public transient Set<KeyBindingConfigEntry> registeredKeyBinds = new ConcurrentSet<>();

        // general
        public KeyBindingConfigEntry pushToTalk = new KeyBindingConfigEntry();
        public KeyBindingConfigEntry priorityPushToTalk = new KeyBindingConfigEntry();
        public KeyBindingConfigEntry muteMicrophone = new KeyBindingConfigEntry();
        public KeyBindingConfigEntry muteChat = new KeyBindingConfigEntry();
        public KeyBindingConfigEntry action = new KeyBindingConfigEntry();

        // distance
        public KeyBindingConfigEntry increaseDistance = new KeyBindingConfigEntry();
        public KeyBindingConfigEntry decreaseDistance = new KeyBindingConfigEntry();

        // sound occlusion
        public KeyBindingConfigEntry occlusion = new KeyBindingConfigEntry();

        public transient Set<InputConstants.Key> pressed = new ConcurrentSet<>();

        public void setupDefaults() {
            pushToTalk.setDefault(
                    this,
                    new KeyBinding("key.plasmo_voice.ptt", ImmutableList.of(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_ALT))),
                    "gui.plasmo_voice.general",
                    true
            );
            priorityPushToTalk.setDefault(
                    this,
                    new KeyBinding("key.plasmo_voice.priority_ptt", ImmutableList.of()),
                    "gui.plasmo_voice.general",
                    true
            );
            muteMicrophone.setDefault(
                    this,
                    new KeyBinding("key.plasmo_voice.mute", ImmutableList.of(InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_M))),
                    "gui.plasmo_voice.general",
                    false
            );
            muteChat.setDefault(
                    this,
                    new KeyBinding("key.plasmo_voice.mute_chat", ImmutableList.of()),
                    "gui.plasmo_voice.general",
                    false
            );
            action.setDefault(
                    this,
                    new KeyBinding("key.plasmo_voice.action", ImmutableList.of(InputConstants.Type.MOUSE.getOrCreate(GLFW.GLFW_MOUSE_BUTTON_2))),
                    "gui.plasmo_voice.general",
                    false
            );

            increaseDistance.setDefault(
                    this,
                    new KeyBinding("key.plasmo_voice.distance.increase", ImmutableList.of()),
                    "key.plasmo_voice.distance",
                    false
            );
            decreaseDistance.setDefault(
                    this,
                    new KeyBinding("key.plasmo_voice.distance.decrease", ImmutableList.of()),
                    "key.plasmo_voice.distance",
                    false
            );

            occlusion.setDefault(
                    this,
                    new KeyBinding("key.plasmo_voice.occlusion.toggle", ImmutableList.of()),
                    "key.plasmo_voice.occlusion",
                    false
            );
        }

        private boolean isKeyBindOpened() {
            if (client.screen instanceof VoiceSettingsScreen) {
                VoiceSettingsScreen screen = (VoiceSettingsScreen) client.screen;
                if (screen.getActiveTab() instanceof KeyBindingsTabWidget) {
                    return ((KeyBindingsTabWidget) screen.getActiveTab()).getFocusedBinding() != null;
                }
            }

            return false;
        }

        public void resetKeys() {
            pressed.clear();
            for (KeyBindingConfigEntry entry : registeredKeyBinds) {
                entry.get().reset();
            }
        }

        public void onKeyDown(InputConstants.Key key) {
            if (isKeyBindOpened()) {
                return;
            }

            pressed.add(key);
            for (KeyBindingConfigEntry entry : registeredKeyBinds) {
                if (entry.anyContext || (client.screen == null || client.screen.passEvents)) {
                    entry.get().onKeyDown(key);
                }
            }
        }

        public void onKeyUp(InputConstants.Key key) {
            if (isKeyBindOpened()) {
                return;
            }

            pressed.removeIf(k -> k.getType().equals(key.getType()) && k.getValue() == key.getValue());
            for (KeyBindingConfigEntry entry : registeredKeyBinds) {
                if (entry.anyContext || (client.screen == null || client.screen.passEvents)) {
                    entry.get().onKeyUp(key);
                }
            }
        }
    }

    public static class KeyBindingConfigEntry extends ConfigEntry<KeyBinding> implements JsonDeserializer<KeyBindingConfigEntry>,
            JsonSerializer<KeyBindingConfigEntry> {
        @Getter
        private boolean anyContext;

        @Override
        public void reset() {
            this.value = new KeyBinding(defaultValue.getTranslation().getKey(), ImmutableList.copyOf(defaultValue.getKeys()));
        }

        public void setDefault(ConfigKeyBindings keyBindings, KeyBinding value, String category, boolean anyContext) {
            this.anyContext = anyContext;
            this.defaultValue = value;
            if (this.value == null) {
                this.value = new KeyBinding(value.getTranslation().getKey(), ImmutableList.copyOf(value.getKeys()));
            } else {
                this.value.setTranslation(value.getTranslation());
            }

            if (!keyBindings.categories.contains(category)) {
                keyBindings.categories.add(category);
            }

            List<KeyBindingConfigEntry> list = keyBindings.categoryEntries.getOrDefault(category, new ArrayList<>());
            if (!list.contains(this)) {
                list.add(this);
            }
            keyBindings.categoryEntries.put(category, list);
            keyBindings.registeredKeyBinds.add(this);
        }

        @Override
        public boolean isDefault() {
            if (getDefault().getKeys().size() != get().getKeys().size()) {
                return false;
            }

            for (int i = 0; i < getDefault().getKeys().size(); i++) {
                if (!getDefault().getKeys().get(i).equals(get().getKeys().get(i))) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public KeyBindingConfigEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            KeyBindingConfigEntry entry = new KeyBindingConfigEntry();
            try {
                List<InputConstants.Key> keys = new ArrayList<>();
                for (JsonElement element : json.getAsJsonArray()) {
                    JsonObject obj = element.getAsJsonObject();
                    keys.add(InputConstants.Type.valueOf(obj.get("type").getAsString()).getOrCreate(obj.get("code").getAsInt()));
                }
                entry.set(new KeyBinding(null, keys));
            } catch (UnsupportedOperationException ignored) {}
            return entry;
        }

        @Override
        public JsonElement serialize(KeyBindingConfigEntry src, Type typeOfSrc, JsonSerializationContext context) {
            if (src.get() == null) {
                return null;
            }

            JsonArray keyBindings = new JsonArray();
            for (InputConstants.Key key : src.get().getKeys()) {
                JsonObject obj = new JsonObject();
                obj.add("type", new JsonPrimitive(key.getType().name()));
                obj.add("code", new JsonPrimitive(key.getValue()));

                keyBindings.add(obj);
            }

            return keyBindings;
        }
    }

    public static class KeyBinding {
        @Getter
        @Setter
        private TranslatableComponent translation;
        @Getter
        @Setter
        private List<InputConstants.Key> keys;

        @Getter
        private boolean pressed;
        @Setter
        private KeyBindingPress onPress;

        public KeyBinding(String translation, List<InputConstants.Key> keys) {
            this.translation = new TranslatableComponent(translation);
            this.keys = keys;
        }

        public void reset() {
            pressed = false;
        }

        public void onKeyDown(InputConstants.Key key) {
            if (keys.size() > 0 && VoiceClient.getClientConfig().keyBindings.pressed.containsAll(keys)) {
                pressed = true;
                if (onPress != null) {
                    onPress.onPress(1);
                }
            }
        }

        public void onKeyUp(InputConstants.Key key) {
            if (pressed && keys.size() > 0 && !VoiceClient.getClientConfig().keyBindings.pressed.containsAll(keys)) {
                pressed = false;
                if (onPress != null) {
                    onPress.onPress(0);
                }
            }
        }

        public interface KeyBindingPress {
            void onPress(int action);
        }
    }

    public static class MicrophoneIconPositionConfigEntry extends ConfigEntry<MicrophoneIconPosition>
            implements JsonDeserializer<MicrophoneIconPositionConfigEntry>,
            JsonSerializer<MicrophoneIconPositionConfigEntry> {
        @Override
        public MicrophoneIconPositionConfigEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            MicrophoneIconPositionConfigEntry entry = new MicrophoneIconPositionConfigEntry();
            try {
                entry.set(MicrophoneIconPosition.valueOf(json.getAsString()));
            } catch (UnsupportedOperationException ignored) {}
            return entry;
        }

        @Override
        public JsonElement serialize(MicrophoneIconPositionConfigEntry src, Type typeOfSrc, JsonSerializationContext context) {
            return src.get() == null ? null : new JsonPrimitive(src.get().toString());
        }
    }
}
