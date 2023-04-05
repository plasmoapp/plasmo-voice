package su.plo.voice.api.proxy.config;

import org.jetbrains.annotations.NotNull;

/**
 * Plasmo Voice Proxy config
 */
public interface ProxyConfig {

    byte[] aesEncryptionKey();

    @NotNull String defaultLanguage();

    boolean debug();

    boolean disableCrowdin();

    boolean checkForUpdates();

    @NotNull Host host();

    interface Host {

        @NotNull String ip();

        int port();
    }
}
