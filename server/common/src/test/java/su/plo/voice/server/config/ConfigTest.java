package su.plo.voice.server.config;

import org.junit.jupiter.api.Test;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigTest {
    @Test
    public void test() {
        ConfigurationProvider toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

        try {
            VoiceServerConfig config = toml.load(VoiceServerConfig.class, new File("test.toml"), true);
            System.out.println(config);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
