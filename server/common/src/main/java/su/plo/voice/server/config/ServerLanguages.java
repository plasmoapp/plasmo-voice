package su.plo.voice.server.config;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.lib.server.command.MinecraftChatHolder;
import su.plo.voice.server.BaseVoiceServer;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

public final class ServerLanguages {

    protected static final ConfigurationProvider toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

    private final BaseVoiceServer voiceServer;
    private final File languagesFolder;

    private final Map<String, ServerLanguage> languages = Maps.newHashMap();
    @Getter
    private ServerLanguage defaultLanguage;

    public ServerLanguages(@NotNull BaseVoiceServer voiceServer) {
        this.voiceServer = voiceServer;
        this.languagesFolder = new File(voiceServer.getConfigFolder(), "languages");
    }

    public void load() {

        try {
            this.defaultLanguage = loadLanguage("en_us");
            languages.put("en_us", defaultLanguage);

            try (InputStream is = voiceServer.getResource("languages/list");
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))
            ) {
                for (String languageName : br.lines().collect(Collectors.toList())) {
                    languages.put(languageName, loadLanguage(languageName));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load languages", e);
        }
    }

    public @NotNull ServerLanguage getLanguage(@NotNull String language) {
        return languages.getOrDefault(language, defaultLanguage);
    }

    public @NotNull ServerLanguage getLanguage(@NotNull MinecraftChatHolder chatHolder) {
        return languages.getOrDefault(chatHolder.getLanguage(), defaultLanguage);
    }

    private ServerLanguage loadLanguage(@NotNull String languageName) throws IOException {
        try (InputStream defaults = voiceServer.getResource(String.format("languages/%s.toml", languageName))) {
            ServerLanguage language;

            File languageFile = new File(languagesFolder, languageName + ".toml");
            if (!languageFile.exists()) {
                language = toml.load(ServerLanguage.class, defaults);
            } else {
                language = toml.load(ServerLanguage.class, languageFile, defaults);
            }

            toml.save(ServerLanguage.class, language, languageFile);
            return language;
        }
    }
}
