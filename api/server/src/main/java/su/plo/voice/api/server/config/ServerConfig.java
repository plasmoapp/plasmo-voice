package su.plo.voice.api.server.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Plasmo Voice Server config
 */
public interface ServerConfig {

    @NotNull String serverId();

    @NotNull String defaultLanguage();

    @NotNull Host host();

    @NotNull Voice voice();

    interface Host {

        @NotNull String ip();

        int port();

        @Nullable Public hostPublic();

        interface Public {

            @NotNull String ip();

            int port();
        }
    }

    interface Voice {

        /**
         * Gets the aes encryption key
         * <br/>
         * Can be changed if server is behind the proxy,
         * so don't store reference to this in addons
         *
         * @return the aes encryption key
         */
        byte[] aesEncryptionKey();

        int sampleRate();

        int keepAliveTimeoutMs();

        int mtuSize();

        boolean clientModRequired();

        @NotNull Proximity proximity();

        @NotNull Opus opus();

        interface Proximity {

            Collection<Integer> distances();

            int defaultDistance();
        }

        interface Opus {

            @NotNull String mode();

            int bitrate();
        }
    }
}
