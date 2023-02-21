package su.plo.voice.server.config;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.toml.Toml;
import su.plo.config.toml.TomlWriter;
import su.plo.voice.api.server.config.ResourceLoader;
import su.plo.voice.api.server.config.ServerLanguages;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class VoiceServerLanguages implements ServerLanguages {

    private final String defaultLanguageName;

    private final Map<String, VoiceServerLanguage> languages = Maps.newHashMap();

    @Override
    public synchronized void register(@NotNull ResourceLoader resourceLoader,
                                      @NotNull File languagesFolder) {
        try {
            Map<String, VoiceServerLanguage> languages = Maps.newHashMap();

            // load from languages/list
            try (InputStream is = resourceLoader.load("languages/list");
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
            ) {
                for (String languageName : br.lines().collect(Collectors.toList())) {
                    if (languageName.isEmpty()) continue;

                    File languageFile = new File(languagesFolder, languageName + ".toml");
                    VoiceServerLanguage language = loadLanguage(resourceLoader, languageFile, languageName);

                    languages.put(languageName, language);
                }
            }

            VoiceServerLanguage defaultLanguage = languages.get(defaultLanguageName);
            if (defaultLanguage == null && languages.size() > 0) {
                defaultLanguage = languages.get(languages.keySet().iterator().next());
            }

            // load from languagesFolder if not found in list and use default language as defaults
            languagesFolder.mkdirs();

            for (File file : languagesFolder.listFiles()) {
                if (file.isDirectory()) continue;

                String fileName = file.getName();
                if (!fileName.endsWith(".toml")) continue;

                String languageName = fileName.substring(0, fileName.length() - 5);

                if (languages.containsKey(languageName)) continue;

                VoiceServerLanguage language = loadLanguage(file, null);
                if (defaultLanguage != null) language.merge(defaultLanguage);

                languages.put(languageName, language);
            }

            // save all languages
            for (Map.Entry<String, VoiceServerLanguage> entry : languages.entrySet()) {
                File languageFile = new File(languagesFolder, entry.getKey() + ".toml");
                saveLanguage(languageFile, entry.getValue());
            }

            // merge all languages with default language
            for (Map.Entry<String, VoiceServerLanguage> entry : languages.entrySet()) {
                VoiceServerLanguage language = this.languages.computeIfAbsent(entry.getKey(), (key) -> entry.getValue());
                language.merge(entry.getValue());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load languages", e);
        }
    }

    @Override
    public Map<String, String> getServerLanguage(@Nullable String languageName) {
        return getLanguage(languageName, "server");
    }

    @Override
    public Map<String, String> getClientLanguage(@Nullable String languageName) {
        return getLanguage(languageName, "client");
    }

    private Map<String, String> getLanguage(@Nullable String languageName, @NotNull String scope) {
        VoiceServerLanguage language = languages.get(languageName == null ? defaultLanguageName : languageName.toLowerCase());
        if (languageName == null && language == null) return ImmutableMap.of();
        if (language == null) return getLanguage(null, scope);

        return scope.equals("server") ? language.getServerLanguage() : language.getClientLanguage();
    }

    private VoiceServerLanguage loadLanguage(@NotNull ResourceLoader resourceLoader,
                                             @NotNull File languageFile,
                                             @NotNull String languageName) throws IOException {
        try (InputStream is = resourceLoader.load("languages/" + languageName + ".toml")) {
            Toml defaults = new Toml().read(new InputStreamReader(is, Charsets.UTF_8));
            return loadLanguage(languageFile, defaults);
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Failed to load language " + languageName, e);
        }
    }

    private VoiceServerLanguage loadLanguage(@NotNull File languageFile,
                                             @Nullable Toml defaults) throws IOException {
        Toml language = new Toml();

        if (languageFile.exists()) {
            try {
                language.read(languageFile);
            } catch (Exception e) {
                throw new IOException("Failed to load language " + languageFile.getName(), e);
            }
        }

        return new VoiceServerLanguage(language, defaults);
    }

    private void saveLanguage(@NotNull File languageFile,
                              @NotNull VoiceServerLanguage language) throws IOException {
        try {
            new TomlWriter().write(language.getOriginal(), languageFile);
        } catch (Exception e) {
            throw new IOException("Failed to save language", e);
        }
    }

    private static class VoiceServerLanguage {

        @Getter
        private Map<String, Object> original;
        @Getter
        private final Map<String, String> serverLanguage;
        @Getter
        private final Map<String, String> clientLanguage;

        public VoiceServerLanguage(@NotNull Toml language,
                                   @Nullable Toml defaults) {
            this.original = defaults == null
                    ? language.toMap()
                    : mergeMaps(language.toMap(), defaults.toMap());

            this.serverLanguage = mergeMaps(language, "server", defaults);
            this.clientLanguage = mergeMaps(language, "client", defaults);
        }

        public void merge(@NotNull VoiceServerLanguage language) {
            this.original = mergeMaps(original, language.original);

            language.serverLanguage.forEach(serverLanguage::putIfAbsent);
            language.clientLanguage.forEach(clientLanguage::putIfAbsent);
        }

        private Map<String, Object> mergeMaps(@NotNull Map<String, Object> language,
                                              @NotNull Map<String, Object> defaults) {
            Map<String, Object> merged = Maps.newConcurrentMap();
            merged.putAll(language);

            for (Map.Entry<String, Object> entry : defaults.entrySet()) {
                if (entry.getValue() instanceof Map) {
                    merged.put(entry.getKey(), mergeMaps(
                            (Map<String, Object>) language.getOrDefault(entry.getKey(), Maps.newHashMap()),
                            (Map<String, Object>) entry.getValue()
                    ));
                } else if (!language.containsKey(entry.getKey())) {
                    merged.put(entry.getKey(), entry.getValue());
                }
            }

            return merged;
        }

        private Map<String, String> mergeMaps(@NotNull Toml language,
                                              @NotNull String scope,
                                              @Nullable Toml defaults) {
            Map<String, String> defaultsMap = Maps.newConcurrentMap();
            if (defaults != null) {
                defaultsMap.putAll(languageToMapOfStrings(
                        defaults.getTable(scope) == null ? new Toml() : defaults.getTable(scope)
                ));
            }

            Map<String, String> languageMap = languageToMapOfStrings(
                    language.getTable(scope) == null ? new Toml() : language.getTable(scope)
            );

            defaultsMap.putAll(languageMap);
            return defaultsMap;
        }

        private Map<String, String> languageToMapOfStrings(@NotNull Toml language) {
            Map<String, String> languageMap = Maps.newHashMap();

            language.toMap().forEach((key, value) -> {
                if (value instanceof Map) {
                    Map<String, String> tableContents = languageToMapOfStrings(language.getTable(key));

                    tableContents.forEach((contentKey, contentValue) -> {
                        languageMap.put(key + "." + contentKey, contentValue);
                    });
                } else {
                    languageMap.put(key, value.toString());
                }
            });

            return languageMap;
        }
    }
}
