package su.plo.voice.proxy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.slib.api.language.ServerTranslator;
import su.plo.slib.api.proxy.channel.McProxyChannelManager;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.addon.ServerAddonsLoader;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.proxy.PlasmoVoiceProxy;
import su.plo.voice.api.proxy.connection.UdpProxyConnectionManager;
import su.plo.voice.api.proxy.event.config.VoiceProxyConfigReloadedEvent;
import su.plo.voice.api.proxy.event.socket.UdpProxyServerCreateEvent;
import su.plo.voice.api.proxy.server.RemoteServerManager;
import su.plo.voice.api.proxy.socket.UdpProxyServer;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.audio.line.ProxySourceLineManager;
import su.plo.voice.proto.data.audio.codec.opus.OpusDecoderInfo;
import su.plo.voice.proto.data.audio.codec.opus.OpusEncoderInfo;
import su.plo.voice.proto.data.audio.codec.opus.OpusMode;
import su.plo.voice.proxy.config.VoiceProxyConfig;
import su.plo.voice.proxy.connection.ProxyChannelHandler;
import su.plo.voice.proxy.connection.ProxyServiceChannelHandler;
import su.plo.voice.proxy.connection.VoiceUdpProxyConnectionManager;
import su.plo.voice.proxy.player.VoiceProxyPlayerManager;
import su.plo.voice.proxy.server.VoiceRemoteServer;
import su.plo.voice.proxy.server.VoiceRemoteServerManager;
import su.plo.voice.proxy.socket.NettyUdpProxyServer;
import su.plo.voice.proxy.util.AddressUtil;
import su.plo.voice.server.audio.capture.VoiceServerActivationManager;
import su.plo.voice.server.audio.line.VoiceProxySourceLineManager;
import su.plo.voice.server.language.VoiceServerLanguages;
import su.plo.voice.server.player.LuckPermsListener;
import su.plo.voice.util.version.ModrinthLoader;
import su.plo.voice.util.version.ModrinthVersion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public abstract class BaseVoiceProxy extends BaseVoice implements PlasmoVoiceProxy {

    public static final String CHANNEL_STRING = "plasmo:voice/v2";
    public static final String SERVICE_CHANNEL_STRING = "plasmo:voice/v2/service";

    protected static final ConfigurationProvider TOML = ConfigurationProvider.getProvider(TomlConfiguration.class);

    @Getter
    private final UdpProxyConnectionManager udpConnectionManager = new VoiceUdpProxyConnectionManager(this);
    @Getter
    private final RemoteServerManager remoteServerManager = new VoiceRemoteServerManager(this);

    @Getter
    private VoiceProxyConfig config;
    @Getter
    private VoiceServerLanguages languages;
    private UdpProxyServer udpProxyServer;
    @Getter
    protected VoiceProxyPlayerManager playerManager;
    @Getter
    protected ProxySourceLineManager sourceLineManager;
    @Getter
    protected ServerActivationManager activationManager;

    protected LuckPermsListener luckPermsListener;

    @Getter
    private Encryption defaultEncryption;

    protected BaseVoiceProxy(@NotNull ModrinthLoader loader) {
        super(loader);

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

        McProxyChannelManager channelManager = getMinecraftServer().getChannelManager();
        channelManager.registerChannelHandler(CHANNEL_STRING, new ProxyChannelHandler(this));
        channelManager.registerChannelHandler(SERVICE_CHANNEL_STRING, new ProxyServiceChannelHandler(this));

        if (LuckPermsListener.Companion.hasLuckPerms()) {
            this.luckPermsListener = new LuckPermsListener(this, backgroundExecutor);
            luckPermsListener.subscribe();
            LOGGER.info("LuckPerms permissions listener attached");
        }

        loadConfig(false);

        checkForUpdates();
    }

    @Override
    protected void onShutdown() {
        getMinecraftServer().getChannelManager().clear();

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

    protected void onProxyConfigReload() {
        loadConfig(true);
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

            ServerTranslator serverTranslator = getMinecraftServer().getServerTranslator();
            this.languages = new VoiceServerLanguages(serverTranslator, config.useCrowdinTranslations());
            serverTranslator.setDefaultLanguage(config.defaultLanguage());
            serverTranslator.setFormat(config.languageFormat());
//            languages.register(
//                    this::getResource,
//                    new File(getConfigFolder(), "languages"),
//                    ServerLanguage.class,
//                    "en_us"
//            );

            // load forwarding secret
            UUID forwardingSecret;
            File forwardingSecretFile = System.getenv().containsKey("PLASMO_VOICE_FORWARDING_SECRET_FILE")
                    ? new File(System.getenv("PLASMO_VOICE_FORWARDING_SECRET_FILE"))
                    : new File(getConfigFolder(), "forwarding-secret");
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
            byte[] aesKey;
            if (oldConfig != null && oldConfig.aesEncryptionKey() != null) {
                aesKey = oldConfig.aesEncryptionKey();
            } else {
                UUID aesEncryptionKey = UUID.randomUUID();
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeLong(aesEncryptionKey.getMostSignificantBits());
                out.writeLong(aesEncryptionKey.getLeastSignificantBits());

                aesKey = out.toByteArray();
            }

            updateAesEncryptionKey(aesKey);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config", e);
        }

        DEBUG_LOGGER.enabled(config.debug() || System.getProperty("plasmovoice.debug") != null);
        if (reload) eventBus.fire(new VoiceProxyConfigReloadedEvent(this, config));
        else addons.initializeLoadedAddons();

        if (restartUdpServer) startUdpServer();
        loadServers();
    }

    private void checkForUpdates() {
        if (config.checkForUpdates()) {
            backgroundExecutor.execute(() -> {
                try {
                    ModrinthVersion.checkForUpdates(getVersion(), "1.19.3", loader)
                            .ifPresent(version -> LOGGER.warn(
                                    "New version available {}: {}",
                                    version.version(),
                                    version.downloadLink())
                            );
                } catch (IOException e) {
                    LOGGER.error("Failed to check for updates", e);
                }
            });
        }
    }

    private void loadServers() {
        remoteServerManager.clear();
        for (Map.Entry<String, String> entry : config.servers().entrySet()) {
            String name = entry.getKey();
            String address = entry.getValue();

            if (getMinecraftServer().getServerInfoByName(name) == null) {
                LOGGER.warn("Server {} not found", name);
                continue;
            }

            try {
                remoteServerManager.register(new VoiceRemoteServer(name, AddressUtil.parseAddress(address)));
            } catch (Exception e) {
                LOGGER.error("Server {} has invalid address {}", name, address, e);
            }
        }
    }

    private void startUdpServer() {
        UdpProxyServer server = new NettyUdpProxyServer(this);

        UdpProxyServerCreateEvent createUdpServerEvent = new UdpProxyServerCreateEvent(server);
        eventBus.fire(createUdpServerEvent);

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
            LOGGER.error("Failed to start the udp server", e);
        }
    }

    private synchronized void updateAesEncryptionKey(byte[] aesKey) {
        config.aesEncryptionKey(aesKey);

        if (this.defaultEncryption == null) {
            this.defaultEncryption = encryption.create("AES/CBC/PKCS5Padding", aesKey);
        } else if (defaultEncryption.getName().equals("AES/CBC/PKCS5Padding")) {
            defaultEncryption.updateKeyData(aesKey);
        }
    }

    @Override
    public Optional<UdpProxyServer> getUdpProxyServer() {
        return Optional.ofNullable(udpProxyServer);
    }

    @Override
    public @NotNull AudioEncoder createOpusEncoder(boolean stereo) {
        if (config == null) throw new IllegalStateException("proxy is not initialized yet");

        return codecs.createEncoder(
                new OpusEncoderInfo(
                        OpusMode.VOIP,
                        -1000
                ),
                config.sampleRate(),
                stereo,
                config.mtuSize()
        );
    }

    @Override
    public @NotNull AudioDecoder createOpusDecoder(boolean stereo) {
        if (config == null) throw new IllegalStateException("proxy is not initialized yet");

        return codecs.createDecoder(
                new OpusDecoderInfo(),
                config.sampleRate(),
                stereo,
                (config.sampleRate() / 1_000) * 20
        );
    }
}
