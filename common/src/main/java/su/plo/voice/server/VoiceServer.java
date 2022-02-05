package su.plo.voice.server;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.plo.voice.server.config.*;
import su.plo.voice.server.metrics.Metrics;
import su.plo.voice.server.network.ServerNetworkHandler;
import su.plo.voice.server.socket.SocketServerUDP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class VoiceServer {
    @Getter
    protected static ServerNetworkHandler network;

    public static final String VERSION = "1.0.9";

    @Getter
    @Setter
    private static MinecraftServer server;

    @Getter
    private static final PlayerManager playerManager = new PlayerManager();

    @Getter
    private static VoiceServer instance;
    public static final Logger LOGGER = LogManager.getLogger("Plasmo Voice");
    public static final ResourceLocation PLASMO_VOICE = new ResourceLocation("plasmo:voice");
    public static final UUID NIL_UUID = new UUID(0, 0);

    // protocol version
    public static final String rawVersion = "1.0.0";
    public static final int version = calculateVersion(rawVersion);
    public static final String rawMinVersion = "0.0.6";
    public static final int minVersion = calculateVersion(rawMinVersion);

    @Getter
    @Setter
    private static ServerConfig serverConfig;
    private SocketServerUDP udpServer;

    @Getter
    protected Configuration config;

    @Getter
    private static final ConcurrentHashMap<UUID, ServerMuted> muted = new ConcurrentHashMap<>();

    private Metrics metrics;

    protected void setupMetrics(String software) {
        metrics = new Metrics(10928, software.toLowerCase(), server);
        metrics.addCustomChart(new Metrics.SingleLineChart("players_with_forge_mod", () ->
                (int) SocketServerUDP.clients.values().stream().filter(s -> s.getType().equals("forge")).count()));
        metrics.addCustomChart(new Metrics.SingleLineChart("players_with_fabric_mod", () ->
                (int) SocketServerUDP.clients.values().stream().filter(s -> s.getType().equals("fabric")).count()));

        metrics.addCustomChart(new Metrics.SimplePie("server_type", () -> software));
    }

    protected void start() {
        instance = this;
        loadConfig();
        updateConfig();

        udpServer = new SocketServerUDP(serverConfig.getIp(), serverConfig.getPort());
        udpServer.start();
    }

    protected void close() {
        if (udpServer != null) {
            udpServer.close();
        }

        if (metrics != null) {
            metrics.close();
        }

        ServerNetworkHandler.playerToken.clear();

        saveData(false);
    }

    public static void saveData(boolean async) {
        if (async) {
            ServerData.saveAsync(new ServerData(new ArrayList<>(muted.values()), playerManager.getPermissions()));
        } else {
            ServerData.save(new ServerData(new ArrayList<>(muted.values()), playerManager.getPermissions()));
        }
    }

    public static int calculateVersion(String s) {
        int ver = 0;
        String[] version = s.split("\\.");
        try {
            ver += Integer.parseInt(version[0]) * 1000;
            ver += Integer.parseInt(version[1]) * 100;
            ver += Integer.parseInt(version[2]);
        } catch (NumberFormatException ignored) {
        }

        return ver;
    }

    public void loadConfig() {
        try {
            // Save default config
            File configDir = new File("config/PlasmoVoice");
            configDir.mkdirs();

            File file = new File(configDir, "server.yml");

            if (!file.exists()) {
                try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("server.yml")) {
                    Files.copy(in, file.toPath());
                }
            }

            Configuration defaults;
            try (InputStream in = this.getClass().getClassLoader().getResourceAsStream("server.yml")) {
                defaults = ConfigurationProvider.getProvider(YamlConfiguration.class)
                        .load(in);
            }

            // Load config
            config = ConfigurationProvider.getProvider(YamlConfiguration.class)
                    .load(file, defaults);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ServerData serverData = ServerData.read();
        if (serverData != null) {
            for (ServerMuted e : serverData.getMuted()) {
                muted.put(e.getUuid(), e);
            }

            playerManager.getPermissions().putAll(serverData.getPermissions());
        }
    }

    public void updateConfig() {
        if (!config.getDefault("config_version").equals(config.getString("config_version"))) {
            LOGGER.warn("Config outdated. Check https://github.com/plasmoapp/plasmo-voice/wiki/How-to-install-Server for new config.");
        }

        int sampleRate = config.getInt("sample_rate");
        if (sampleRate != 8000 && sampleRate != 12000 && sampleRate != 24000 && sampleRate != 48000) {
            LOGGER.warn("Sample rate cannot be " + sampleRate);
            return;
        }

        List<Integer> distances = config.getIntList("distances");
        if (distances.size() == 0) {
            LOGGER.warn("Distances cannot be empty");
            return;
        }
        int defaultDistance = config.getInt("default_distance");
        if (defaultDistance == 0) {
            defaultDistance = distances.get(0);
        } else if (!distances.contains(defaultDistance)) {
            defaultDistance = distances.get(0);
        }

        int fadeDivisor = config.getInt("fade_divisor");
        if (fadeDivisor <= 0) {
            LOGGER.warn("Fade distance cannot be <= 0");
            return;
        }

        int priorityFadeDivisor = config.getInt("priority_fade_divisor");
        if (priorityFadeDivisor <= 0) {
            LOGGER.warn("Priority fade distance cannot be <= 0");
            return;
        }

        int udpPort = config.getInt("udp.port");
        if (udpPort == 0) {
            udpPort = server.getPort();

            // integrated server, use 60606
            if (udpPort == -1) {
                udpPort = 60606;
            }
        }

        serverConfig = new ServerConfig(
                config.getString("udp.ip"),
                udpPort,
                config.getString("udp.proxy_ip"),
                config.getInt("udp.proxy_port"),
                sampleRate,
                distances,
                defaultDistance,
                config.getInt("max_priority_distance"),
                config.getBoolean("disable_voice_activation"),
                fadeDivisor,
                priorityFadeDivisor);
    }

    public static boolean isLogsEnabled() {
        return !VoiceServer.getInstance().getConfig().getBoolean("disable_logs");
    }

    // Get message with prefix from config
    public String getMessagePrefix(String name) {
        // Get message or use default value
        String message = config.getString("messages." + name);
        if (message == null) {
            message = "";
        } else {
            message = message.replace('&', '\u00A7');
        }

        return this.getPrefix() + message;
    }

    // Get message from config
    public String getMessage(String name) {
        // Get message or use default value
        String message = config.getString("messages." + name);
        if (message == null) {
            message = "";
        } else {
            message = message.replace('&', '\u00A7');
        }

        return message;
    }

    // Get plugin prefix from config
    public String getPrefix() {
        return config.getString("messages.prefix").replace('&', '\u00A7');
    }
}
