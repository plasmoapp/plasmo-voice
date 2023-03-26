package su.plo.voice.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.addon.ServerAddonsLoader;
import su.plo.voice.api.logging.DebugLogger;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.connection.UdpProxyConnectionManager;
import su.plo.voice.api.proxy.event.VoiceProxyShutdownEvent;
import su.plo.voice.api.proxy.event.config.VoiceProxyConfigReloadedEvent;
import su.plo.voice.api.proxy.event.socket.UdpProxyServerCreateEvent;
import su.plo.voice.api.proxy.server.RemoteServerManager;
import su.plo.voice.api.proxy.socket.UdpProxyServer;
import su.plo.voice.api.server.PlasmoBaseVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.audio.line.ProxySourceLineManager;
import su.plo.voice.proxy.config.VoiceProxyConfig;
import su.plo.voice.proxy.connection.VoiceUdpProxyConnectionManager;
import su.plo.voice.proxy.player.VoiceProxyPlayerManager;
import su.plo.voice.proxy.server.VoiceRemoteServer;
import su.plo.voice.proxy.server.VoiceRemoteServerManager;
import su.plo.voice.proxy.socket.NettyUdpProxyServer;
import su.plo.voice.proxy.util.AddressUtil;
import su.plo.voice.server.audio.capture.VoiceServerActivationManager;
import su.plo.voice.server.audio.line.VoiceProxySourceLineManager;
import su.plo.voice.server.config.VoiceServerLanguages;
import su.plo.voice.server.player.LuckPermsListener;
import su.plo.voice.server.player.PermissionSupplier;
import su.plo.voice.util.version.ModrinthLoader;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class BaseVoiceProxy extends BaseVoice implements PlasmoVoiceProxy {

    public static final String CHANNEL_STRING = "plasmo:voice/v2";
    public static final String SERVICE_CHANNEL_STRING = "plasmo:voice/proxy/v2";

    protected static final ConfigurationProvider TOML = ConfigurationProvider.getProvider(TomlConfiguration.class);

    @Getter
    protected final DebugLogger debugLogger = new DebugLogger(logger);
    @Getter
    private final UdpProxyConnectionManager udpConnectionManager = new VoiceUdpProxyConnectionManager(this);
    @Getter
    private final RemoteServerManager remoteServerManager = new VoiceRemoteServerManager();

    @Getter
    private VoiceProxyConfig config;
    private UdpProxyServer udpProxyServer;
    @Getter
    protected VoiceProxyPlayerManager playerManager;
    @Getter
    protected ProxySourceLineManager sourceLineManager;
    @Getter
    protected ServerActivationManager activationManager;
    @Getter
    protected VoiceServerLanguages languages;

    protected LuckPermsListener luckPermsListener;

    protected BaseVoiceProxy(@NotNull ModrinthLoader loader) {
        super(
                loader,
                LogManager.getLogger("PlasmoVoiceProxy")
        );

        ServerAddonsLoader.INSTANCE.setAddonManager(getAddonManager());
    }

    @Override
    protected void onInitialize() {
        this.playerManager = new VoiceProxyPlayerManager(this, getMinecraftServer());

        super.onInitialize();

        this.sourceLineManager = new VoiceProxySourceLineManager(this);
        this.activationManager = new VoiceServerActivationManager(
                this,
                playerManager,
                null
        );
        eventBus.register(this, activationManager);

        eventBus.register(this, udpConnectionManager);
        eventBus.register(this, playerManager);
        eventBus.register(this, getMinecraftServer());

        if (LuckPermsListener.Companion.hasLuckPerms()) {
            this.luckPermsListener = new LuckPermsListener(this, backgroundExecutor);
            luckPermsListener.subscribe();
            logger.info("LuckPerms permissions listener attached");
        }

        loadConfig(false);
    }

    @Override
    protected void onShutdown() {
        eventBus.call(new VoiceProxyShutdownEvent(this));

        if (luckPermsListener != null) {
            luckPermsListener.unsubscribe();
            this.luckPermsListener = null;
        }

        if (udpProxyServer != null) {
            udpProxyServer.stop();
            this.udpProxyServer = null;
        }

        eventBus.unregister(this);
        super.onShutdown();
    }

    public void loadConfig(boolean reload) {
        boolean restartUdpServer = true;

        try {
            File configFile = new File(getConfigFolder(), "config.toml");
            VoiceProxyConfig oldConfig = config;

            this.config = TOML.load(VoiceProxyConfig.class, configFile, false);
            TOML.save(VoiceProxyConfig.class, config, configFile);

            if (oldConfig != null) {
                restartUdpServer = !config.host().equals(oldConfig.host());
            }

            this.languages = new VoiceServerLanguages(config.defaultLanguage());
//            languages.register(
//                    this::getResource,
//                    new File(getConfigFolder(), "languages"),
//                    ServerLanguage.class,
//                    "en_us"
//            );

            // load forwarding secret
            UUID forwardingSecret;
            File forwardingSecretFile = new File(getConfigFolder(), "forwarding-secret");
            if (System.getenv("PLASMO_VOICE_FORWARDING_SECRET") != null) {
                forwardingSecret = UUID.fromString(System.getenv("PLASMO_VOICE_FORWARDING_SECRET"));
            } else if (forwardingSecretFile.exists()) {
                forwardingSecret = UUID.fromString(new String(Files.readAllBytes(forwardingSecretFile.toPath())));
            } else {
                forwardingSecret = UUID.randomUUID();
                Files.write(forwardingSecretFile.toPath(), forwardingSecret.toString().getBytes(StandardCharsets.UTF_8));
            }
            config.forwardingSecret(forwardingSecret);

            // load AES key
            if (oldConfig != null && oldConfig.aesEncryptionKey() != null) {
                config.aesEncryptionKey(oldConfig.aesEncryptionKey());
            } else {
                UUID aesEncryptionKey = UUID.randomUUID();
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeLong(aesEncryptionKey.getMostSignificantBits());
                out.writeLong(aesEncryptionKey.getLeastSignificantBits());

                config.aesEncryptionKey(out.toByteArray());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config", e);
        }

        debugLogger.enabled(config.debug() || System.getProperty("plasmovoice.debug") != null);
        if (reload) eventBus.call(new VoiceProxyConfigReloadedEvent(this));
        else addons.initializeLoadedAddons();

        if (restartUdpServer) startUdpServer();
        loadServers();
    }

    private void loadServers() {
        remoteServerManager.clear();
        for (Map.Entry<String, String> entry : config.servers().entrySet()) {
            String name = entry.getKey();
            String address = entry.getValue();

            if (!getMinecraftServer().getServerByName(name).isPresent()) {
                getLogger().warn("Server {} not found", name);
                continue;
            }

            try {
                remoteServerManager.register(new VoiceRemoteServer(name, AddressUtil.parseAddress(address)));
            } catch (Exception e) {
                getLogger().error("Server {} has invalid address {}", name, address, e);
            }
        }
    }

    private void startUdpServer() {
        UdpProxyServer server = new NettyUdpProxyServer(this);

        UdpProxyServerCreateEvent createUdpServerEvent = new UdpProxyServerCreateEvent(server);
        eventBus.call(createUdpServerEvent);

        server = createUdpServerEvent.getServer();

        try {
            int port = config.host().port();
            if (port == 0) {
                port = getMinecraftServer().getPort();
                if (port <= 0) port = 0;
            }

            server.start(config.host().ip(), port);
            this.udpProxyServer = server;
        } catch (Exception e) {
            getLogger().error("Failed to start the udp server", e);
        }
    }

    @Override
    public Optional<UdpProxyServer> getUdpProxyServer() {
        return Optional.ofNullable(udpProxyServer);
    }

    @Override
    public Module createInjectModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(PlasmoVoiceProxy.class).toInstance(BaseVoiceProxy.this);
                bind(PlasmoBaseVoiceServer.class).toInstance(BaseVoiceProxy.this);
            }
        };
    }

    protected abstract PermissionSupplier createPermissionSupplier();
}
