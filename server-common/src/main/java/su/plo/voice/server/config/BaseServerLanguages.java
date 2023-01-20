package su.plo.voice.server.config;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.lib.api.server.command.MinecraftChatHolder;
import su.plo.voice.api.server.config.ResourceLoader;
import su.plo.voice.api.server.config.ServerLanguages;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseServerLanguages implements ServerLanguages {

    private static final ConfigurationProvider toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

    private final Map<Class<?>, ServerLanguageContainer> languages = Maps.newHashMap();

    @Override
    public void register(@NotNull ResourceLoader resourceLoader,
                         @NotNull File languagesFolder,
                         @NotNull Class<?> languageClass,
                         @NotNull String defaultLanguageName) {
        if (languages.containsKey(languageClass)) {
            throw new IllegalStateException("Language " + languageClass + " already registered");
        }

        try {
            Object defaultLanguage = loadLanguage(resourceLoader, languagesFolder, languageClass, defaultLanguageName);
            ServerLanguageContainer language = new ServerLanguageContainer(defaultLanguage, defaultLanguageName);

            try (InputStream is = resourceLoader.load("languages/list");
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))
            ) {
                for (String languageName : br.lines().collect(Collectors.toList())) {
                    if (languageName.isEmpty() || languageName.equals(defaultLanguageName)) {
                        continue;
                    }

                    language.getLanguages().put(
                            languageName,
                            loadLanguage(resourceLoader, languagesFolder, languageClass, languageName)
                    );
                }
            }

            languages.put(languageClass, language);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load languages", e);
        }
    }

    @Override
    public <T> T getLanguage(@NotNull Class<?> languageClass, @NotNull String languageName) {
        ServerLanguageContainer language = languages.get(languageClass);
        if (language == null) {
            throw new IllegalStateException("Language " + languageClass + " not registered");
        }

        return (T) language.getLanguages().getOrDefault(languageName, language.getDefaultLanguage());
    }

    @Override
    public <T> T getLanguage(@NotNull Class<?> languageClass, @NotNull MinecraftChatHolder chatHolder) {
        return getLanguage(languageClass, chatHolder.getLanguage());
    }

    private Object loadLanguage(@NotNull ResourceLoader resourceLoader,
                                @NotNull File languagesFolder,
                                @NotNull Class<?> languageClass,
                                @NotNull String languageName) throws IOException {
        try (InputStream defaults = resourceLoader.load(String.format("languages/%s.toml", languageName))) {
            Object language;

            File languageFile = new File(languagesFolder, languageName + ".toml");
            if (!languageFile.exists()) {
                language = toml.load(languageClass, defaults);
            } else {
                language = toml.load(languageClass, languageFile, defaults);
            }

            toml.save(languageClass, language, languageFile);
            return language;
        }
    }
}
