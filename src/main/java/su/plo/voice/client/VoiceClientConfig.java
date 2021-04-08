package su.plo.voice.client;

import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.HashMap;

public class VoiceClientConfig {
    public HashMap<String, VoiceClientServerConfig> servers = new HashMap<>();
    public boolean occlusion = false;
    public int showIcons = 0;
    public boolean voiceActivation = false;
    public double voiceActivationThreshold = 0.0D;
    public double voiceVolume = 1.0D;
    public double microphoneAmplification = 1.0D;
    public String microphone;
    public String speaker;

    public VoiceClientConfig() {}

    public void load() {
        File configFile = new File("config/PlasmoVoice/config.json");
        if(configFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(configFile));
                VoiceClientConfig config = VoiceClient.gson.fromJson(reader, VoiceClientConfig.class);
                if(config != null) {
                    this.voiceVolume = config.voiceVolume;
                    this.microphoneAmplification = config.microphoneAmplification;
                    this.microphone = config.microphone;
                    this.speaker = config.speaker;
                    this.occlusion = config.occlusion;
                    this.showIcons = config.showIcons;
                    this.servers = config.servers;
                    this.voiceActivation = config.voiceActivation;
                    this.voiceActivationThreshold = config.voiceActivationThreshold;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        // async write
        new Thread(() -> {
            File configDir = new File("config/PlasmoVoice");
            configDir.mkdirs();

            try {
                try(Writer w = new FileWriter("config/PlasmoVoice/config.json")) {
                    w.write(VoiceClient.gson.toJson(this));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
