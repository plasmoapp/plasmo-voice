package su.plo.voice.server.config;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.*;
import java.util.*;

@AllArgsConstructor
@Data
public class ServerData {
    private final static Gson gson = new Gson();

    private final List<ServerMuted> muted;
    private final Map<UUID, Map<String, Boolean>> permissions;

    public static ServerData read() {
        File dataFile = new File("config/PlasmoVoice/server_data.json");
        if(dataFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(dataFile));
                try {
                    return gson.fromJson(reader, ServerData.class);
                } catch (JsonSyntaxException j) {
                    dataFile.delete();
                }
            } catch (FileNotFoundException ignored) {}
        }

        return new ServerData(new ArrayList<>(), new HashMap<>());
    }

    public static void saveAsync(ServerData data) {
        new Thread(() -> {
            File configDir = new File("config/PlasmoVoice");
            configDir.mkdirs();

            try {
                try (Writer w = new FileWriter("config/PlasmoVoice/server_data.json")) {
                    w.write(gson.toJson(data));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void save(ServerData data) {
        File configDir = new File("config/PlasmoVoice");
        configDir.mkdirs();

        try {
            try (Writer w = new FileWriter("config/PlasmoVoice/server_data.json")) {
                w.write(gson.toJson(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
