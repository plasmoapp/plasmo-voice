package su.plo.voice.server.config;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class ServerLanguageContainer {

    @Getter
    private final Object defaultLanguage;
    @Getter
    private final Map<String, Object> languages = Maps.newHashMap();

    public ServerLanguageContainer(@NotNull Object defaultLanguage,
                                   @NotNull String defaultLanguageName) {
        this.defaultLanguage = defaultLanguage;
        languages.put(defaultLanguageName, defaultLanguage);
    }
}
