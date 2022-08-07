package su.plo.voice.client.config;

import org.junit.jupiter.api.Test;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;

import java.io.File;
import java.io.IOException;

public class ClientConfigTest {

    @Test
    public void load() {
        ConfigurationProvider toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

        try {
            File file = new File("client.toml");

            ClientConfig config = toml.load(ClientConfig.class, file, true);

            System.out.println(config);
            toml.save(ClientConfig.class, config, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
