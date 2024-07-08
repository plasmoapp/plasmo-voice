package su.plo.voice.api.proxy.config;

import org.jetbrains.annotations.NotNull;
import su.plo.slib.api.language.ServerLanguageFormat;

/**
 * Proxy configuration for Plasmo Voice.
 */
public interface ProxyConfig {

    byte[] aesEncryptionKey();

    @NotNull String defaultLanguage();

    @NotNull ServerLanguageFormat languageFormat();

    boolean debug();

    boolean useCrowdinTranslations();

    boolean checkForUpdates();

    int mtuSize();

    @NotNull Host host();

    interface Host {

        @NotNull String ip();

        int port();
    }
}
