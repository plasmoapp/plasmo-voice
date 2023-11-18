package su.plo.voice.server;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.slib.api.command.McCommand;
import su.plo.slib.api.command.McCommandManager;
import su.plo.slib.api.language.ServerTranslator;
import su.plo.slib.api.permission.PermissionDefault;
import su.plo.slib.api.permission.PermissionManager;
import su.plo.slib.api.server.McServerLib;
import su.plo.slib.api.server.channel.McServerChannelManager;
import su.plo.slib.api.server.event.command.McServerCommandsRegisterEvent;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.addon.ServerAddonsLoader;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.server.PlasmoBaseVoiceServer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.audio.line.ServerSourceLineManager;
import su.plo.voice.api.server.connection.TcpServerPacketManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.event.config.VoiceServerConfigReloadedEvent;
import su.plo.voice.api.server.event.socket.UdpServerCreateEvent;
import su.plo.voice.api.server.event.socket.UdpServerStartedEvent;
import su.plo.voice.api.server.event.socket.UdpServerStoppedEvent;
import su.plo.voice.api.server.mute.MuteManager;
import su.plo.voice.api.server.mute.storage.MuteStorage;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.proto.data.audio.codec.opus.OpusDecoderInfo;
import su.plo.voice.proto.data.audio.codec.opus.OpusEncoderInfo;
import su.plo.voice.proto.data.audio.codec.opus.OpusMode;
import su.plo.voice.server.audio.capture.ProximityServerActivation;
import su.plo.voice.server.audio.capture.VoiceServerActivationManager;
import su.plo.voice.server.audio.line.VoiceServerSourceLineManager;
import su.plo.voice.server.command.*;
import su.plo.voice.server.config.VoiceServerConfig;
import su.plo.voice.server.connection.ServerChannelHandler;
import su.plo.voice.server.connection.ServerServiceChannelHandler;
import su.plo.voice.server.connection.VoiceTcpServerConnectionManager;
import su.plo.voice.server.connection.VoiceUdpServerConnectionManager;
import su.plo.voice.server.language.VoiceServerLanguages;
import su.plo.voice.server.mute.VoiceMuteManager;
import su.plo.voice.server.mute.storage.MuteStorageFactory;
import su.plo.voice.server.player.LuckPermsListener;
import su.plo.voice.server.player.VoiceServerPlayerManagerImpl;
import su.plo.voice.server.socket.NettyUdpServer;
import su.plo.voice.util.version.ModrinthLoader;
import su.plo.voice.util.version.ModrinthVersion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class BaseVoiceServer extends BaseVoice implements PlasmoVoiceServer {

    public static final String CHANNEL_STRING = "plasmo:voice/v2";
    public static final String FLAG_CHANNEL_STRING = "plasmo:voice/v2/installed";
    public static final String SERVICE_CHANNEL_STRING = "plasmo:voice/v2/service";

    public static final UUID USTATS_PROJECT_UUID = UUID.fromString("2b87ab55-5ec8-48ef-b8bb-a5018e53d65e");

    protected static final ConfigurationProvider TOML = ConfigurationProvider.getProvider(TomlConfiguration.class);

    @Getter
    protected final TcpServerPacketManager tcpPacketManager = new VoiceTcpServerConnectionManager(this);
    @Getter
    protected final UdpServerConnectionManager udpConnectionManager = new VoiceUdpServerConnectionManager(this);

    protected UdpServer udpServer;
    @Getter
    protected VoiceServerPlayerManagerImpl playerManager;
    @Getter
    protected ServerActivationManager activationManager;
    protected final ProximityServerActivation proximityActivation = new ProximityServerActivation(this);
    @Getter
    protected ServerSourceLineManager sourceLineManager;

    @Getter
    protected MuteStorage muteStorage;
    @Getter
    protected MuteManager muteManager;

    protected LuckPermsListener luckPermsListener;

    @Getter
    protected VoiceServerConfig config;
    @Getter
    protected VoiceServerLanguages languages;

    @Getter
    private Encryption defaultEncryption;

    private final ServerChannelHandler channelHandler = new ServerChannelHandler(this);
    private final ServerServiceChannelHandler serviceChannelHandler = new ServerServiceChannelHandler(this);

    protected BaseVoiceServer(@NotNull ModrinthLoader loader) {
        super(loader);

        ServerAddonsLoader.INSTANCE.setAddonManager(getAddonManager());
        McServerCommandsRegisterEvent.INSTANCE.registerListener(this::registerDefaultCommandsAndPermissions);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        McServerChannelManager channelManager = getMinecraftServer().getChannelManager();
        channelManager.registerChannelHandler(CHANNEL_STRING, channelHandler);
        channelManager.registerChannelHandler(SERVICE_CHANNEL_STRING, serviceChannelHandler);

        eventBus.register(this, udpConnectionManager);
        eventBus.register(this, getMinecraftServer());
        eventBus.register(this, proximityActivation);

        this.playerManager = new VoiceServerPlayerManagerImpl(this, getMinecraftServer());
        playerManager.registerPermission("pv.allow_freecam");
        eventBus.register(this, playerManager);

        this.activationManager = new VoiceServerActivationManager(
                this,
                tcpPacketManager,
                (activationName) -> config.voice().weights().getActivationWeight(activationName)
        );
        eventBus.register(this, activationManager);
        this.sourceLineManager = new VoiceServerSourceLineManager(this);

        // mutes
        MuteStorageFactory muteStorageFactory = new MuteStorageFactory(this, backgroundExecutor);
        this.muteStorage = muteStorageFactory.createStorage("json");

        try {
            this.muteStorage.init();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize mute storage: {}", e.toString());
            e.printStackTrace();
            return;
        }

        this.muteManager = new VoiceMuteManager(this, this.muteStorage, backgroundExecutor);

        if (LuckPermsListener.Companion.hasLuckPerms()) {
            this.luckPermsListener = new LuckPermsListener(this, backgroundExecutor);
            luckPermsListener.subscribe();
            LOGGER.info("LuckPerms permissions listener attached");
        }

        loadConfig(false);

        // check for updates
        checkForUpdates();
    }

    @Override
    protected void onShutdown() {
        if (luckPermsListener != null) {
            luckPermsListener.unsubscribe();
            this.luckPermsListener = null;
        }

        if (muteStorage != null) {
            try {
                muteStorage.close();
            } catch (Exception e) {
                LOGGER.error("Failed to close mute storage: {}", e.toString());
                e.printStackTrace();
            }
        }

        stopUdpServer();

        // cleanup
        sourceLineManager.clear();
        activationManager.clear();
        playerManager.clear();
        channelHandler.clear();

        this.config = null;

        eventBus.unregister(this);
        super.onShutdown();
    }

    public void loadConfig(boolean reload) {
        boolean restartUdpServer = true;

        try {
            File configFile = new File(getConfigFolder(), "config.toml");
            VoiceServerConfig oldConfig = config;

            this.config = TOML.load(VoiceServerConfig.class, configFile, false);
            TOML.save(VoiceServerConfig.class, config, configFile);

            if (oldConfig != null) {
                restartUdpServer = !config.host().equals(oldConfig.host());
            }

            ServerTranslator serverTranslator = getMinecraftServer().getServerTranslator();
            this.languages = new VoiceServerLanguages(serverTranslator, config.useCrowdinTranslations());
            languages.register(
                    "plasmo-voice",
                    "server.toml",
                    this::getResource,
                    new File(getConfigFolder(), "languages")
            );
            serverTranslator.setDefaultLanguage(config.defaultLanguage());

            // load forwarding secret
            File forwardingSecretFile = System.getenv().containsKey("PLASMO_VOICE_FORWARDING_SECRET_FILE")
                    ? new File(System.getenv("PLASMO_VOICE_FORWARDING_SECRET_FILE"))
                    : new File(getConfigFolder(), "forwarding-secret");
            if (System.getenv("PLASMO_VOICE_FORWARDING_SECRET") != null) {
                UUID forwardingSecret = UUID.fromString(System.getenv("PLASMO_VOICE_FORWARDING_SECRET"));
                config.host().forwardingSecret(forwardingSecret);
            } else if (forwardingSecretFile.exists()) {
                UUID forwardingSecret = UUID.fromString(new String(Files.readAllBytes(forwardingSecretFile.toPath())));
                config.host().forwardingSecret(forwardingSecret);
            }

            // load server id from ENV
            if (System.getenv("PLASMO_VOICE_SERVER_ID") != null) {
                config.serverId(System.getenv("PLASMO_VOICE_SERVER_ID"));
            }

            // load AES key
            byte[] aesKey;
            if (oldConfig != null && oldConfig.voice().aesEncryptionKey() != null) {
                aesKey = oldConfig.voice().aesEncryptionKey();
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

        // register proximity activation
        proximityActivation.register(config);

        if (reload) eventBus.fire(new VoiceServerConfigReloadedEvent(this, config));
        else addons.initializeLoadedAddons();

        if (restartUdpServer) startUdpServer();
    }

    public synchronized void updateAesEncryptionKey(byte[] aesKey) {
        config.voice().aesEncryptionKey(aesKey);

        if (this.defaultEncryption == null) {
            this.defaultEncryption = encryption.create("AES/CBC/PKCS5Padding", aesKey);
        } else if (defaultEncryption.getName().equals("AES/CBC/PKCS5Padding")) {
            defaultEncryption.updateKeyData(aesKey);
        }
    }

    private void startUdpServer() {
        Collection<VoiceServerPlayer> connectedPlayers = null;
        if (this.udpServer != null) {
            connectedPlayers = udpConnectionManager.getConnections()
                    .stream()
                    .map(UdpServerConnection::getPlayer)
                    .collect(Collectors.toList());
            stopUdpServer();
        }

        UdpServer server = new NettyUdpServer(this);

        UdpServerCreateEvent createUdpServerEvent = new UdpServerCreateEvent(server);
        eventBus.fire(createUdpServerEvent);

        server = createUdpServerEvent.getServer();

        try {
            int port = config.host().port();
            if (port == 0) {
                port = getMinecraftServer().getPort();
                if (port <= 0) port = 0;
            }

            server.start(config.host().ip(), port);
            this.udpServer = server;
            eventBus.fire(new UdpServerStartedEvent(server));

            if (connectedPlayers != null) {
                connectedPlayers.forEach(tcpPacketManager::requestPlayerInfo);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to start the udp server", e);
        }
    }

    private void stopUdpServer() {
        if (this.udpServer != null) {
            this.udpServer.stop();
            eventBus.fire(new UdpServerStoppedEvent(udpServer));
            this.udpServer = null;
        }
    }

    private void checkForUpdates() {
        if (config.checkForUpdates()) {
            backgroundExecutor.execute(() -> {
                try {
                    ModrinthVersion.checkForUpdates(getVersion(), getMinecraftServer().getVersion(), loader)
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

    protected void registerDefaultCommandsAndPermissions(
            @NotNull McCommandManager<McCommand> commandManager,
            @NotNull McServerLib minecraftServer
    ) {
        // register permissions
        PermissionManager permissions = minecraftServer.getPermissionManager();

        permissions.register("pv.list", PermissionDefault.TRUE);
        permissions.register("pv.reconnect", PermissionDefault.TRUE);

        permissions.register("pv.allow_freecam", PermissionDefault.TRUE);

        // register commands
        commandManager.register("vlist", new VoiceListCommand(this));
        commandManager.register("vrc", new VoiceReconnectCommand(this));
        commandManager.register("vreload", new VoiceReloadCommand(this));

        commandManager.register("vmute", new VoiceMuteCommand(this, getMinecraftServer()));
        commandManager.register("vunmute", new VoiceUnmuteCommand(this, getMinecraftServer()));
        commandManager.register("vmutelist", new VoiceMuteListCommand(this, getMinecraftServer()));
    }

    @Override
    public Optional<UdpServer> getUdpServer() {
        return Optional.ofNullable(udpServer);
    }

    @Override
    public Map<Class<?>, Object> createInjectModule() {
        Map<Class<?>, Object> injectModule = Maps.newHashMap();
        injectModule.put(PlasmoVoiceServer.class, BaseVoiceServer.this);
        injectModule.put(PlasmoBaseVoiceServer.class, BaseVoiceServer.this);
        return injectModule;
    }

    @Override
    public @NotNull AudioEncoder createOpusEncoder(boolean stereo) {
        if (config == null) throw new IllegalStateException("server is not initialized yet");

        int sampleRate = config.voice().sampleRate();

        return codecs.createEncoder(
                new OpusEncoderInfo(
                        OpusMode.valueOf(config.voice().opus().mode()),
                        config.voice().opus().bitrate()
                ),
                sampleRate,
                stereo,
                config.voice().mtuSize()
        );
    }

    @Override
    public @NotNull AudioDecoder createOpusDecoder(boolean stereo) {
        if (config == null) throw new IllegalStateException("server is not initialized yet");

        int sampleRate = config.voice().sampleRate();
        return codecs.createDecoder(
                new OpusDecoderInfo(),
                sampleRate,
                stereo,
                (sampleRate / 1_000) * 20
        );
    }
}
