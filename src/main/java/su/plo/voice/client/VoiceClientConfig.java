package su.plo.voice.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import su.plo.voice.data.DataEntity;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

@Data
public class VoiceClientConfig {
    private final static Gson gson = new Gson();

    @Setter(AccessLevel.PRIVATE)
    private HashMap<String, VoiceClientServerConfig> servers = new HashMap<>();
    private boolean occlusion = false;
    private int showIcons = 0;
    private MicrophoneIconPosition micIconPosition = MicrophoneIconPosition.BOTTOM_CENTER;
    private boolean voiceActivation = false;
    private double voiceActivationThreshold = 0.0D;
    private double voiceVolume = 1.0D;
    private double microphoneAmplification = 1.0D;
    private String microphone;
    private String speaker;
    private boolean whitelist = false;

    // Stores in another file
    @Setter(AccessLevel.PRIVATE)
    private transient HashSet<UUID> muted = new HashSet<>();
    @Setter(AccessLevel.PRIVATE)
    private transient HashSet<UUID> whitelisted = new HashSet<>();

    public VoiceClientConfig() {}

    public static VoiceClientConfig read() {
        VoiceClientConfig config = null;
        File configFile = new File("config/PlasmoVoice/config.json");
        if(configFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(configFile));
                config = gson.fromJson(reader, VoiceClientConfig.class);
            } catch (FileNotFoundException ignored) {} catch (JsonSyntaxException e) {
                configFile.delete();
            }
        }

        if(config == null) {
            config = new VoiceClientConfig();
        }

        DataEntity data = DataEntity.read();
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

            (new DataEntity(muted, whitelisted)).save();
        }).start();
    }

    // mute stuff
    public boolean isMuted(UUID uuid) {
        if(whitelist) {
            return !whitelisted.contains(uuid);
        } else {
            return muted.contains(uuid);
        }
    }

    public void mute(UUID uuid) {
        if(whitelist) {
            whitelisted.remove(uuid);
        } else {
            muted.add(uuid);
        }
    }

    public void unmute(UUID uuid) {
        if(whitelist) {
            whitelisted.add(uuid);
        } else {
            muted.remove(uuid);
        }
    }
}
