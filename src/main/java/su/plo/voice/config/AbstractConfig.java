package su.plo.voice.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import su.plo.voice.config.entries.BooleanConfigEntry;
import su.plo.voice.config.entries.DoubleConfigEntry;
import su.plo.voice.config.entries.IntegerConfigEntry;
import su.plo.voice.config.entries.StringConfigEntry;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public abstract class AbstractConfig {
    public static Gson gson;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(BooleanConfigEntry.class, new BooleanConfigEntry());
        builder.registerTypeAdapter(IntegerConfigEntry.class, new IntegerConfigEntry(0, 0));
        builder.registerTypeAdapter(DoubleConfigEntry.class, new DoubleConfigEntry(0, 0));
        builder.registerTypeAdapter(StringConfigEntry.class, new StringConfigEntry());
        gson = builder.create();
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
    }

    protected abstract void setupDefaults();
}
