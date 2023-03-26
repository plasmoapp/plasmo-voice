package su.plo.voice.server;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.lib.api.server.event.command.ServerCommandsRegisterEvent;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.lib.api.server.permission.PermissionsManager;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.addon.ServerAddonsLoader;
import su.plo.voice.api.audio.codec.AudioDecoder;
import su.plo.voice.api.audio.codec.AudioEncoder;
import su.plo.voice.api.encryption.Encryption;
import su.plo.voice.api.logging.DebugLogger;
import su.plo.voice.api.server.PlasmoBaseVoiceServer;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.audio.line.ServerSourceLineManager;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.event.VoiceServerShutdownEvent;
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
import su.plo.voice.server.config.VoiceServerLanguages;
import su.plo.voice.server.connection.VoiceTcpServerConnectionManager;
import su.plo.voice.server.connection.VoiceUdpServerConnectionManager;
import su.plo.voice.server.mute.VoiceMuteManager;
import su.plo.voice.server.mute.storage.MuteStorageFactory;
import su.plo.voice.server.player.LuckPermsListener;
import su.plo.voice.server.player.PermissionSupplier;
import su.plo.voice.server.player.VoiceServerPlayerManager;
import su.plo.voice.server.socket.NettyUdpServer;
import su.plo.voice.util.version.ModrinthLoader;
import su.plo.voice.util.version.ModrinthVersion;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class BaseVoiceServer extends BaseVoice implements PlasmoVoiceServer {

    public static final String CHANNEL_STRING = "plasmo:voice/v2";
    public static final String SERVICE_CHANNEL_STRING = "plasmo:voice/proxy/v2";

    protected static final ConfigurationProvider TOML = ConfigurationProvider.getProvider(TomlConfiguration.class);

    @Getter
    protected final DebugLogger debugLogger = new DebugLogger(logger);
    @Getter
    protected final TcpServerConnectionManager tcpConnectionManager = new VoiceTcpServerConnectionManager(this);
    @Getter
    protected final UdpServerConnectionManager udpConnectionManager = new VoiceUdpServerConnectionManager(this);

    protected UdpServer udpServer;
    @Getter
    protected PermissionSupplier permissionSupplier;
    @Getter
    protected VoiceServerPlayerManager playerManager;
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

    protected BaseVoiceServer(@NotNull ModrinthLoader loader) {
        super(
                loader,
                LogManager.getLogger("PlasmoVoiceServer")
        );

        ServerAddonsLoader.INSTANCE.setAddonManager(getAddonManager());
    }

    @Override
    protected void onInitialize() {
        // check for updates
        try {
            ModrinthVersion.checkForUpdates(getVersion(), getMinecraftServer().getVersion(), loader)
                    .ifPresent(version -> logger.warn(
                            "New version available {}: {}",
                            version.version(),
                            version.downloadLink())
                    );
        } catch (IOException e) {
            logger.error("Failed to check for updates", e);
        }

        super.onInitialize();

        eventBus.register(this, udpConnectionManager);
        eventBus.register(this, getMinecraftServer());
        eventBus.register(this, proximityActivation);

        this.permissionSupplier = createPermissionSupplier();

        this.playerManager = new VoiceServerPlayerManager(this, getMinecraftServer());
        eventBus.register(this, playerManager);

        this.activationManager = new VoiceServerActivationManager(
                this,
                tcpConnectionManager,
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
            getLogger().error("Failed to initialize mute storage: {}", e.toString());
            e.printStackTrace();
            return;
        }

        this.muteManager = new VoiceMuteManager(this, this.muteStorage, backgroundExecutor);

        if (LuckPermsListener.Companion.hasLuckPerms()) {
            this.luckPermsListener = new LuckPermsListener(this, backgroundExecutor);
            luckPermsListener.subscribe();
            logger.info("LuckPerms permissions listener attached");
        }

        loadConfig(false);
    }

    @Override
    protected void onShutdown() {
        eventBus.call(new VoiceServerShutdownEvent(this));

        if (luckPermsListener != null) {
            luckPermsListener.unsubscribe();
            this.luckPermsListener = null;
        }

        if (muteStorage != null) {
            try {
                muteStorage.close();
            } catch (Exception e) {
                getLogger().error("Failed to close mute storage: {}", e.toString());
                e.printStackTrace();
            }
        }

        stopUdpServer();

        // cleanup
        sourceLineManager.clear();
        activationManager.clear();
        playerManager.clear();

        this.config = null;
        this.languages = null;

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

            this.languages = new VoiceServerLanguages(config.defaultLanguage());
            languages.register(
                    "plasmo-voice",
                    "server.toml",
                    this::getResource,
                    new File(getConfigFolder(), "languages")
            );

            // load forwarding secret
            File forwardingSecretFile = new File(getConfigFolder(), "forwarding-secret");
            if (System.getenv("PLASMO_VOICE_FORWARDING_SECRET") != null) {
                UUID forwardingSecret = UUID.fromString(System.getenv("PLASMO_VOICE_FORWARDING_SECRET"));
                config.host().forwardingSecret(forwardingSecret);
            } else if (forwardingSecretFile.exists()) {
                UUID forwardingSecret = UUID.fromString(new String(Files.readAllBytes(forwardingSecretFile.toPath())));
                config.host().forwardingSecret(forwardingSecret);
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

        debugLogger.enabled(config.debug() || System.getProperty("plasmovoice.debug") != null);

        // register proximity activation
        proximityActivation.register(config);

        if (reload) eventBus.call(new VoiceServerConfigReloadedEvent(this, config));
        else addons.initializeLoadedAddons();

        if (restartUdpServer) startUdpServer();
    }

    public void updateAesEncryptionKey(byte[] aesKey) {
        config.voice().aesEncryptionKey(aesKey);
        // initialize default encoder
        this.defaultEncryption = encryption.create("AES/CBC/PKCS5Padding", aesKey);
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
        eventBus.call(createUdpServerEvent);

        server = createUdpServerEvent.getServer();

        try {
            int port = config.host().port();
            if (port == 0) {
                port = getMinecraftServer().getPort();
                if (port <= 0) port = 0;
            }

            server.start(config.host().ip(), port);
            this.udpServer = server;
            eventBus.call(new UdpServerStartedEvent(server));

            if (connectedPlayers != null) {
                connectedPlayers.forEach(tcpConnectionManager::requestPlayerInfo);
            }
        } catch (Exception e) {
            getLogger().error("Failed to start the udp server", e);
        }
    }

    private void stopUdpServer() {
        if (this.udpServer != null) {
            this.udpServer.stop();
            eventBus.call(new UdpServerStoppedEvent(udpServer));
            this.udpServer = null;
        }
    }

    protected void registerDefaultCommandsAndPermissions() {
        // register permissions
        PermissionsManager permissions = getMinecraftServer().getPermissionsManager();
        permissions.clear();

        permissions.register("pv.list", PermissionDefault.TRUE);
        permissions.register("pv.reconnect", PermissionDefault.TRUE);

        // register commands
        MinecraftCommandManager<MinecraftCommand> commandManager = getMinecraftServer().getCommandManager();
        commandManager.clear();

        ServerCommandsRegisterEvent.INSTANCE.getInvoker().onCommandsRegister(commandManager, getMinecraftServer());

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
    public Module createInjectModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(PlasmoVoiceServer.class).toInstance(BaseVoiceServer.this);
                bind(PlasmoBaseVoiceServer.class).toInstance(BaseVoiceServer.this);
            }
        };
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
                (sampleRate / 1_000) * 20,
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
                (sampleRate / 1_000) * 20,
                config.voice().mtuSize()
        );
    }

    protected abstract PermissionSupplier createPermissionSupplier();
}
