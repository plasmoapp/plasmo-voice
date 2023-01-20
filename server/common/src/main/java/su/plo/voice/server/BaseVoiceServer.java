package su.plo.voice.server;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.lib.api.server.command.MinecraftCommand;
import su.plo.lib.api.server.command.MinecraftCommandManager;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.lib.api.server.permission.PermissionsManager;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.addon.AddonScope;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.audio.line.ServerSourceLineManager;
import su.plo.voice.api.server.audio.source.ServerSourceManager;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.event.VoiceServerInitializeEvent;
import su.plo.voice.api.server.event.VoiceServerShutdownEvent;
import su.plo.voice.api.server.event.command.CommandsRegisterEvent;
import su.plo.voice.api.server.event.config.VoiceServerConfigLoadedEvent;
import su.plo.voice.api.server.event.mute.MuteStorageCreateEvent;
import su.plo.voice.api.server.event.socket.UdpServerCreateEvent;
import su.plo.voice.api.server.mute.MuteManager;
import su.plo.voice.api.server.mute.storage.MuteStorage;
import su.plo.voice.api.server.player.VoiceServerPlayer;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.api.server.socket.UdpServerConnection;
import su.plo.voice.server.audio.capture.ProximityServerActivation;
import su.plo.voice.server.audio.capture.VoiceServerActivationManager;
import su.plo.voice.server.audio.line.VoiceServerSourceLineManager;
import su.plo.voice.server.audio.source.VoiceServerSourceManager;
import su.plo.voice.server.command.*;
import su.plo.voice.server.config.ServerLanguage;
import su.plo.voice.server.config.VoiceServerConfig;
import su.plo.voice.server.config.VoiceServerLanguages;
import su.plo.voice.server.connection.VoiceTcpConnectionManager;
import su.plo.voice.server.connection.VoiceUdpConnectionManager;
import su.plo.voice.server.mute.VoiceMuteManager;
import su.plo.voice.server.mute.storage.MuteStorageFactory;
import su.plo.voice.server.player.LuckPermsListener;
import su.plo.voice.server.player.PermissionSupplier;
import su.plo.voice.server.player.VoiceServerPlayerManager;
import su.plo.voice.server.socket.NettyUdpServer;

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

    protected final Logger logger = LogManager.getLogger();
    @Getter
    protected final TcpServerConnectionManager tcpConnectionManager = new VoiceTcpConnectionManager(this);
    @Getter
    protected final UdpServerConnectionManager udpConnectionManager = new VoiceUdpConnectionManager(this);
    @Getter
    protected final ServerSourceManager sourceManager = new VoiceServerSourceManager(this);

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

    @Override
    protected void onInitialize() {
        super.onInitialize();

        eventBus.call(new VoiceServerInitializeEvent(this));
        eventBus.register(this, sourceManager);
        eventBus.register(this, udpConnectionManager);
        eventBus.register(this, getMinecraftServer());
        eventBus.register(this, proximityActivation);

        this.permissionSupplier = createPermissionSupplier();

        this.playerManager = new VoiceServerPlayerManager(this, getMinecraftServer());
        eventBus.register(this, playerManager);

        this.activationManager = new VoiceServerActivationManager(this, tcpConnectionManager, playerManager);
        eventBus.register(this, activationManager);
        this.sourceLineManager = new VoiceServerSourceLineManager(tcpConnectionManager, addons);

        // mutes
        MuteStorageFactory muteStorageFactory = new MuteStorageFactory(this, backgroundExecutor);
        MuteStorage muteStorage = muteStorageFactory.createStorage("json");

        MuteStorageCreateEvent muteStorageCreateEvent = new MuteStorageCreateEvent(muteStorage);
        eventBus.call(muteStorageCreateEvent);
        this.muteStorage = muteStorageCreateEvent.getStorage();

        try {
            this.muteStorage.init();
        } catch (Exception e) {
            getLogger().error("Failed to initialize mute storage: {}", e.toString());
            e.printStackTrace();
            return;
        }

        this.muteManager = new VoiceMuteManager(this, this.muteStorage, backgroundExecutor);

        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            this.luckPermsListener = new LuckPermsListener(this, playerManager, backgroundExecutor);
            luckPermsListener.subscribe();
        } catch (IllegalStateException | ClassNotFoundException ignored) {
            // luckperms not found
        }

        // load config
        loadConfig();
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

        if (udpServer != null) {
            udpServer.stop();
            this.udpServer = null;
        }

        // cleanup sources
        sourceManager.clear();

        // cleanup source lines
        sourceLineManager.clear();

        // cleanup activations
        activationManager.clear();

        // cleanup players
        playerManager.clear();

        this.config = null;
        this.languages = null;

        eventBus.unregister(this);
        super.onShutdown();
    }

    public void loadConfig() {
        boolean restartUdpServer = true;

        try {
            File configFile = new File(getConfigFolder(), "config.toml");
            VoiceServerConfig oldConfig = config;

            this.config = TOML.load(VoiceServerConfig.class, configFile, false);
            TOML.save(VoiceServerConfig.class, config, configFile);

            if (oldConfig != null) {
                restartUdpServer = !config.host().equals(oldConfig.host());
            }

            this.languages = new VoiceServerLanguages();
            languages.register(
                    this::getResource,
                    new File(getConfigFolder(), "languages"),
                    ServerLanguage.class,
                    "en_us"
            );

            // load forwarding secret
            // todo: use env
            File forwardingSecretFile = new File(getConfigFolder(), "forwarding-secret");
            if (forwardingSecretFile.exists()) {
                UUID forwardingSecret = UUID.fromString(new String(Files.readAllBytes(forwardingSecretFile.toPath())));
                config.host().forwardingSecret(forwardingSecret);
            }

            // load AES key
            if (oldConfig != null && oldConfig.voice().aesEncryptionKey() != null) {
                config.voice().aesEncryptionKey(oldConfig.voice().aesEncryptionKey());
            } else {
                UUID aesEncryptionKey = UUID.randomUUID();
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeLong(aesEncryptionKey.getMostSignificantBits());
                out.writeLong(aesEncryptionKey.getLeastSignificantBits());

                config.voice().aesEncryptionKey(out.toByteArray());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config", e);
        }

        eventBus.call(new VoiceServerConfigLoadedEvent(this));

        // register proximity activation
        proximityActivation.register(config);

        if (restartUdpServer) startUdpServer();
    }

    private void startUdpServer() {
        Collection<VoiceServerPlayer> connectedPlayers = null;
        if (this.udpServer != null) {
            connectedPlayers = udpConnectionManager.getConnections()
                    .stream()
                    .map(UdpServerConnection::getPlayer)
                    .collect(Collectors.toList());
            this.udpServer.stop();
            this.udpServer = null;
        }

        UdpServer server = new NettyUdpServer(this, config);

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

            if (connectedPlayers != null) {
                connectedPlayers.forEach(tcpConnectionManager::connect);
            }
        } catch (Exception e) {
            getLogger().error("Failed to start the udp server", e);
        }
    }

    protected void registerDefaultCommandsAndPermissions() {
        // load addons
        loadAddons();

        // register permissions
        PermissionsManager permissions = getMinecraftServer().getPermissionsManager();
        permissions.clear();

        permissions.register("pv.list", PermissionDefault.TRUE);
        permissions.register("pv.reconnect", PermissionDefault.TRUE);

        // register commands
        MinecraftCommandManager<MinecraftCommand> commandManager = getMinecraftServer().getCommandManager();
        commandManager.clear();

        eventBus.call(new CommandsRegisterEvent(this, commandManager));

        commandManager.register("vlist", new VoiceListCommand(this));
        commandManager.register("vrc", new VoiceReconnectCommand(this));
        commandManager.register("vreload", new VoiceReloadCommand(this));

        commandManager.register("vmute", new VoiceMuteCommand(this, getMinecraftServer()));
        commandManager.register("vunmute", new VoiceUnmuteCommand(this, getMinecraftServer()));
        commandManager.register("vmutelist", new VoiceMuteListCommand(this, getMinecraftServer()));
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected AddonScope getScope() {
        return AddonScope.SERVER;
    }

    @Override
    public Optional<UdpServer> getUdpServer() {
        return Optional.ofNullable(udpServer);
    }

    protected abstract PermissionSupplier createPermissionSupplier();
}
