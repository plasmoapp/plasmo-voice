package su.plo.voice.server;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.lib.server.MinecraftServerLib;
import su.plo.lib.server.command.MinecraftCommandManager;
import su.plo.lib.server.permission.PermissionDefault;
import su.plo.lib.server.permission.PermissionsManager;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivationManager;
import su.plo.voice.api.server.audio.line.ServerSourceLineManager;
import su.plo.voice.api.server.audio.source.ServerSourceManager;
import su.plo.voice.api.server.connection.TcpServerConnectionManager;
import su.plo.voice.api.server.connection.UdpServerConnectionManager;
import su.plo.voice.api.server.event.VoiceServerConfigLoadedEvent;
import su.plo.voice.api.server.event.VoiceServerInitializeEvent;
import su.plo.voice.api.server.event.VoiceServerShutdownEvent;
import su.plo.voice.api.server.event.mute.MuteStorageCreateEvent;
import su.plo.voice.api.server.event.socket.UdpServerCreateEvent;
import su.plo.voice.api.server.mute.MuteManager;
import su.plo.voice.api.server.mute.storage.MuteStorage;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.api.server.socket.UdpConnection;
import su.plo.voice.api.server.socket.UdpServer;
import su.plo.voice.proto.data.audio.line.VoiceSourceLine;
import su.plo.voice.server.audio.capture.ProximityServerActivation;
import su.plo.voice.server.audio.capture.VoiceServerActivationManager;
import su.plo.voice.server.audio.line.VoiceServerSourceLineManager;
import su.plo.voice.server.audio.source.VoiceServerSourceManager;
import su.plo.voice.server.command.*;
import su.plo.voice.server.config.ServerConfig;
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
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

// todo: refactor server
//  need to initialize it after MinecraftServer is created
public abstract class BaseVoiceServer extends BaseVoice implements PlasmoVoiceServer {

    public static final String CHANNEL_STRING = "plasmo:voice/v2";

    protected static final ConfigurationProvider toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

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
    protected ServerConfig config;

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

        this.activationManager = new VoiceServerActivationManager(this);
        eventBus.register(this, activationManager);
        this.sourceLineManager = new VoiceServerSourceLineManager(this);

        // mutes
        MuteStorageFactory muteStorageFactory = new MuteStorageFactory(this, executor);
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

        this.muteManager = new VoiceMuteManager(this, this.muteStorage, executor);

        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            this.luckPermsListener = new LuckPermsListener(this, playerManager, executor);
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

        eventBus.unregister(this);
        super.onShutdown();
    }

    public void loadConfig() {
        boolean restartUdpServer = true;

        try {
            File configFile = new File(getConfigFolder(), "config.toml");
            ServerConfig oldConfig = config;

            this.config = toml.load(ServerConfig.class, configFile, false);
            toml.save(ServerConfig.class, config, configFile);

            if (oldConfig != null) {
                restartUdpServer = !config.getHost().equals(oldConfig.getHost());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config", e);
        }

        eventBus.call(new VoiceServerConfigLoadedEvent(this));

        // register proximity activation
        proximityActivation.register(config);
        activationManager.register(
                this,
                "groups",
                "Groups",
                "plasmovoice:textures/icons/microphone_group.png",
                ImmutableList.of(),
                0,
                true,
                false,
                3
        );

        // register proximity line
        sourceLineManager.register(
                this,
                VoiceSourceLine.PROXIMITY_NAME,
                "activation.plasmovoice.proximity",
                "plasmovoice:textures/icons/speaker.png",
                1
        );
        sourceLineManager.registerPlayers(
                this,
                "groups",
                "Groups",
                "plasmovoice:textures/icons/speaker_groups.png",
                3
        );

        if (restartUdpServer) startUdpServer();
    }

    public void startUdpServer() {
        Collection<VoicePlayer> connectedPlayers = null;
        if (this.udpServer != null) {
            connectedPlayers = udpConnectionManager.getConnections()
                    .stream()
                    .map(UdpConnection::getPlayer)
                    .collect(Collectors.toList());
            this.udpServer.stop();
            this.udpServer = null;
        }

        UdpServer server = new NettyUdpServer(this, config);

        UdpServerCreateEvent createUdpServerEvent = new UdpServerCreateEvent(server);
        eventBus.call(createUdpServerEvent);

        server = createUdpServerEvent.getServer();

        try {
            int port = config.getHost().getPort();
            if (port == 0) {
                port = getMinecraftServerPort();
                if (port <= 0) port = 0;
            }

            server.start(config.getHost().getIp(), port);
            this.udpServer = server;

            if (connectedPlayers != null) {
                connectedPlayers.forEach(tcpConnectionManager::connect);
            }
        } catch (Exception e) {
            getLogger().error("Failed to start the udp server", e);
        }
    }

    protected void registerDefaultCommandsAndPermissions() {
        // register permissions
        PermissionsManager permissions = getMinecraftServer().getPermissionsManager();
        permissions.clear();

        permissions.register("voice.list", PermissionDefault.TRUE);
        permissions.register("voice.reconnect", PermissionDefault.TRUE);

        // register commands
        MinecraftCommandManager commandManager = getMinecraftServer().getCommandManager();
        commandManager.clear();

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
    protected Addon.Scope getScope() {
        return Addon.Scope.SERVER;
    }

    @Override
    public Optional<UdpServer> getUdpServer() {
        return Optional.ofNullable(udpServer);
    }

    public abstract int getMinecraftServerPort();

    public abstract MinecraftServerLib getMinecraftServer();

    protected abstract PermissionSupplier createPermissionSupplier();
}
