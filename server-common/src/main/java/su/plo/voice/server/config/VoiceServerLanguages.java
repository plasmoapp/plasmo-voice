package su.plo.voice.server.config;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.config.toml.Toml;
import su.plo.config.toml.TomlWriter;
import su.plo.crowdin.PlasmoCrowdinLib;
import su.plo.voice.api.server.config.ResourceLoader;
import su.plo.voice.api.server.config.ServerLanguages;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
                for (String languageName : br.lines().filter(line -> !line.isEmpty()).collect(Collectors.toList())) {
                    File languageFile = new File(languagesFolder, String.format("%s.toml", languageName));

                    VoiceServerLanguage language = loadLanguage(resourceLoader, languageFile, languageName);
                    languages.put(languageName, language);
                }
            }

            register(languages, languagesFolder);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load languages", e);
        }
    }

    @Override
    public void register(@NotNull String crowdinProjectId,
                         @Nullable String fileName,
                         @NotNull ResourceLoader resourceLoader,
                         @NotNull File languagesFolder) {
        try {
            try {
                downloadCrowdinTranslations(crowdinProjectId, fileName, languagesFolder);
            } catch (Exception e) {
                LogManager.getLogger().warn(
                        "Failed to download crowdin project {} translations: {}",
                        crowdinProjectId,
                        e.getMessage()
                );
                e.printStackTrace();
            }

            File crowdinFolder = new File(languagesFolder, ".crowdin");
            if (!crowdinFolder.exists()) {
                register(resourceLoader, languagesFolder);
                return;
            }

            Map<String, VoiceServerLanguage> languages = Maps.newHashMap();

            // load from languages/list
            try (InputStream is = resourceLoader.load("languages/list");
                 BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
            ) {
                for (String languageName : br.lines().filter(line -> !line.isEmpty()).collect(Collectors.toList())) {
                    String languageFileName = String.format("%s.toml", languageName);

                    File crowdinFile = new File(crowdinFolder, languageFileName);
                    File languageFile = new File(languagesFolder, languageFileName);

                    VoiceServerLanguage language = loadLanguage(resourceLoader, crowdinFile, languageFile, languageName);
                    languages.put(languageName, language);
                }
            }

            // merge defaults from jar and from crowdin, map based on crowdin
            File[] crowdinTranslations = crowdinFolder.listFiles();
            if (crowdinTranslations != null) {
                for (File crowdinFile : crowdinTranslations) {
                    if (crowdinFile.getName().equals("timestamp")) continue;

                    String languageName = crowdinFile.getName().split("\\.")[0];
                    if (languages.containsKey(languageName)) continue;

                    File languageFile = new File(languagesFolder, crowdinFile.getName());

                    VoiceServerLanguage language = loadLanguage(resourceLoader, crowdinFile, languageFile, languageName);
                    languages.put(languageName, language);
                }
            }

            register(languages, languagesFolder);
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

    private void downloadCrowdinTranslations(@NotNull String crowdinProjectId,
                                             @Nullable String fileName,
                                             @NotNull File languagesFolder) throws Exception {
        long timestamp = 0L;
        File crowdinFolder = new File(languagesFolder, ".crowdin");
        File timestampFile = new File(crowdinFolder, "timestamp");
        if (timestampFile.exists()) {
            String timestampString = new String(Files.readAllBytes(timestampFile.toPath()));
            try {
                timestamp = Long.parseLong(timestampString);
            } catch (NumberFormatException ignored) {
            }
        }

        // check timestamp, if outdated, download from crowdin and use it as defaults
        if (System.currentTimeMillis() - timestamp < 86_400 * 3 * 1000) return;

        Map<String, byte[]> rawTranslations = PlasmoCrowdinLib.INSTANCE
                .downloadRawTranslations(crowdinProjectId, fileName)
                .get();

        // write timestamp file
        crowdinFolder.mkdirs();
        Files.write(
                timestampFile.toPath(),
                String.valueOf(System.currentTimeMillis()).getBytes()
        );

        if (rawTranslations.isEmpty()) return;

        // write translations files
        for (Map.Entry<String, byte[]> entry : rawTranslations.entrySet()) {
            String languageName = entry.getKey();
            byte[] translationBytes = entry.getValue();

            Files.write(
                    new File(crowdinFolder, String.format("%s.toml", languageName)).toPath(),
                    translationBytes
            );
        }
    }

    private void register(@NotNull Map<String, VoiceServerLanguage> languages,
                          @NotNull File languagesFolder) throws IOException {
        if (languages.size() == 0) return;

        final VoiceServerLanguage defaultLanguage = languages.getOrDefault(
                defaultLanguageName,
                languages.get(languages.keySet().iterator().next())
        );

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

        // merge languages
        for (Map.Entry<String, VoiceServerLanguage> entry : languages.entrySet()) {
            VoiceServerLanguage language = this.languages.computeIfAbsent(entry.getKey(), (key) -> entry.getValue());
            language.merge(entry.getValue());
            language.merge(defaultLanguage);
        }

        this.languages.forEach((languageName, language) -> {
            if (languages.containsKey(languageName)) return;
            language.merge(defaultLanguage);
        });
    }

    private Map<String, String> getLanguage(@Nullable String languageName, @NotNull String scope) {
        VoiceServerLanguage language = languages.get(languageName == null ? defaultLanguageName : languageName.toLowerCase());
        if (languageName == null && language == null) return ImmutableMap.of();
        if (language == null) return getLanguage(null, scope);

        return scope.equals("server") ? language.getServerLanguage() : language.getClientLanguage();
    }

    private VoiceServerLanguage loadLanguage(@NotNull ResourceLoader resourceLoader,
                                             @NotNull File crowdinTranslation,
                                             @NotNull File languageFile,
                                             @NotNull String languageName) throws IOException {
        // defaults are based on crowdin translations then merged with defaults from jar (if exist)
        Toml crowdinDefaults = null;
        if (crowdinTranslation.exists()) {
            try (FileInputStream fis = new FileInputStream(crowdinTranslation)) {
                crowdinDefaults = new Toml().read(new InputStreamReader(fis, Charsets.UTF_8));
            }
        }

        Toml jarDefaults = null;
        try (InputStream is = resourceLoader.load("languages/" + languageName + ".toml")) {
            if (is != null) jarDefaults = new Toml().read(new InputStreamReader(is, Charsets.UTF_8));
        } catch (Exception ignored) {
        }

        if (crowdinDefaults == null && jarDefaults == null) {
            throw new IOException("Both crowdin and jar defaults are null for language " + languageName);
        }

        Toml tomlLanguage = new Toml();
        if (languageFile.exists()) {
            try {
                tomlLanguage.read(languageFile);
            } catch (Exception e) {
                throw new IOException("Failed to load language " + languageFile.getName(), e);
            }
        }

        VoiceServerLanguage language = new VoiceServerLanguage(tomlLanguage, null);
        if (crowdinDefaults != null) language.merge(new VoiceServerLanguage(crowdinDefaults, null));
        if (jarDefaults != null) language.merge(new VoiceServerLanguage(jarDefaults, null));

        return language;
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
