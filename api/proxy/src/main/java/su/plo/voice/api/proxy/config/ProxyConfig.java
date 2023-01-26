package su.plo.voice.api.proxy.config;

import org.jetbrains.annotations.NotNull;

/**
 * Plasmo Voice Proxy config
 */
public interface ProxyConfig {

    byte[] aesEncryptionKey();

    @NotNull String defaultLanguage();

    @NotNull Host host();

    interface Host {

        @NotNull String ip();

        int port();
    }
}
