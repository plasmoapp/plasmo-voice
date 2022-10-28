package su.plo.voice.api.server.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Plasmo Voice Server config
 */
public interface ServerConfig {

    @NotNull String getServerId();

    @NotNull Host getHost();

    @NotNull Voice getVoice();

    interface Host {

        @NotNull String getIp();

        int getPort();

        boolean isProxyProtocol();

        @Nullable Public getHostPublic();

        interface Public {

            @NotNull String getIp();

            int getPort();
        }
    }

    interface Voice {

        int getSampleRate();

        int getKeepAliveTimeoutMs();

        int getMtuSize();

        boolean isClientModRequired();

        @NotNull Proximity getProximity();

        @NotNull Opus getOpus();

        interface Proximity {

            Collection<Integer> getDistances();

            int getDefaultDistance();
        }

        interface Opus {

            @NotNull String getMode();

            int getBitrate();
        }
    }
}
