package su.plo.voice.api.proxy.config;

import org.jetbrains.annotations.NotNull;

/**
 * Proxy configuration for Plasmo Voice.
 */
public interface ProxyConfig {

    byte[] aesEncryptionKey();

    @NotNull String defaultLanguage();

    boolean debug();

    boolean useCrowdinTranslations();

    boolean checkForUpdates();

    int mtuSize();

    @NotNull Host host();

    @NotNull ReusePort reusePort();

    interface Host {

        @NotNull String ip();

        int port();
    }

    interface ReusePort {
        boolean enabled();

        int channels();
    }
}
