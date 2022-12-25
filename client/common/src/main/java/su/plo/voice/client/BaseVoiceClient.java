package su.plo.voice.client;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.api.client.gui.ScreenContainer;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.annotation.Addon;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.api.client.config.addon.AddonConfig;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.connection.UdpClientManager;
import su.plo.voice.api.client.event.VoiceClientInitializedEvent;
import su.plo.voice.api.client.event.VoiceClientShutdownEvent;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.render.DistanceVisualizer;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.client.audio.capture.VoiceAudioCapture;
import su.plo.voice.client.audio.capture.VoiceClientActivationManager;
import su.plo.voice.client.audio.device.JavaxInputDeviceFactory;
import su.plo.voice.client.audio.device.VoiceDeviceFactoryManager;
import su.plo.voice.client.audio.device.VoiceDeviceManager;
import su.plo.voice.client.audio.line.VoiceClientSourceLineManager;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.addon.VoiceAddonConfig;
import su.plo.voice.client.config.keybind.HotkeyActions;
import su.plo.voice.client.connection.VoiceUdpClientManager;
import su.plo.voice.client.gui.PlayerVolumeAction;
import su.plo.voice.client.gui.settings.VoiceNotAvailableScreen;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.render.HudIconRenderer;
import su.plo.voice.client.render.OverlayRenderer;
import su.plo.voice.client.render.SourceIconRenderer;
import su.plo.voice.client.render.VoiceDistanceVisualizer;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public abstract class BaseVoiceClient extends BaseVoice implements PlasmoVoiceClient {

    public static final String CHANNEL_STRING = "plasmo:voice/v2";

    protected static final ConfigurationProvider toml = ConfigurationProvider.getProvider(TomlConfiguration.class);

    protected final Logger logger = LogManager.getLogger("PlasmoVoiceClient");

    @Getter
    private final DeviceFactoryManager deviceFactoryManager = new VoiceDeviceFactoryManager();
    @Getter
    private final UdpClientManager udpClientManager = new VoiceUdpClientManager();

    @Setter
    private ServerInfo serverInfo;

    @Getter
    private DeviceManager deviceManager;
    @Getter
    private AudioCapture audioCapture;
    @Getter
    private ClientActivationManager activationManager;
    @Getter
    private ClientSourceLineManager sourceLineManager;
    @Getter
    private DistanceVisualizer distanceVisualizer;

    @Getter
    protected ClientConfig config;
    @Getter
    protected final Map<String, AddonConfig> addonConfigs = Maps.newHashMap();

    protected VoiceSettingsScreen settingsScreen;

    protected BaseVoiceClient() {
        // JavaX input
        getDeviceFactoryManager().registerDeviceFactory(new JavaxInputDeviceFactory(this));
    }

    public void openSettings() {
        MinecraftClientLib minecraft = getMinecraft();

        Optional<ScreenContainer> screen = minecraft.getScreen();
        if (screen.isPresent() && screen.get().get() instanceof VoiceSettingsScreen) {
            minecraft.setScreen(null);
            return;
        }

        if (!udpClientManager.isConnected()) {
            openNotAvailable();
            return;
        }

        if (settingsScreen == null) {
            this.settingsScreen = new VoiceSettingsScreen(
                    minecraft,
                    this,
                    config
            );
        }
        minecraft.setScreen(settingsScreen);
    }

    public void openNotAvailable() {
        MinecraftClientLib minecraft = getMinecraft();

        VoiceNotAvailableScreen notAvailableScreen = new VoiceNotAvailableScreen(minecraft, this);

        Optional<UdpClient> udpClient = udpClientManager.getClient();
        if (udpClient.isPresent()) {
            if (udpClient.get().isClosed()) {
                notAvailableScreen.setCannotConnect();
            } else {
                notAvailableScreen.setConnecting();
            }
        }

        minecraft.setScreen(notAvailableScreen);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        getMinecraft().onInitialize();

        try {
            File configFile = new File(getConfigFolder(), "client.toml");

            this.config = toml.load(ClientConfig.class, configFile, false);
            toml.save(ClientConfig.class, config, configFile);

            config.setConfigFile(configFile);
            config.setAsyncExecutor(executor);

            eventBus.register(this, config.getKeyBindings());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load the config", e);
        }

        MinecraftClientLib minecraft = getMinecraft();

        this.distanceVisualizer = new VoiceDistanceVisualizer(minecraft, this, config);

        this.deviceManager = new VoiceDeviceManager(this, config);
        this.sourceLineManager = new VoiceClientSourceLineManager(config);
        this.activationManager = new VoiceClientActivationManager(minecraft, this, config);
        this.audioCapture = new VoiceAudioCapture(this, config);

        // hotkey actions
        new HotkeyActions(minecraft, getKeyBindings(), config).register();
        PlayerVolumeAction volumeAction = createPlayerVolumeAction(minecraft);
        eventBus.register(this, volumeAction);

        // render
        eventBus.register(this, distanceVisualizer);
        eventBus.register(this, new HudIconRenderer(minecraft, this, config));
        eventBus.register(this, new SourceIconRenderer(minecraft, this, config, volumeAction));
        eventBus.register(this, new OverlayRenderer(minecraft, this, config));

        getEventBus().call(new VoiceClientInitializedEvent(this));
    }

    @Override
    protected void onShutdown() {
        logger.info("Shutting down");

        if (config != null) config.save(false);

        eventBus.unregister(this);

        getMinecraft().onShutdown();
        super.onShutdown();

        getEventBus().call(new VoiceClientShutdownEvent(this));
    }

    protected void onServerDisconnect() {
        System.out.println("disconnect");
        udpClientManager.removeClient(UdpClientClosedEvent.Reason.DISCONNECT);
        getServerConnection().ifPresent(ServerConnection::close);
        getMinecraft().onServerDisconnect();
    }

    @Override
    protected Logger getLogger() {
        return logger;
    }

    @Override
    protected Addon.Scope getScope() {
        return Addon.Scope.CLIENT;
    }

    @Override
    public Optional<ServerInfo> getServerInfo() {
        return Optional.ofNullable(serverInfo);
    }

    @Override
    public @NotNull KeyBindings getKeyBindings() {
        return config.getKeyBindings();
    }

    @Override
    public synchronized @NotNull AddonConfig getAddonConfig(@NotNull Object addonInstance) {
        AddonContainer addon = addons.getAddon(addonInstance)
                .orElseThrow(() -> new IllegalArgumentException("Addon not found"));

        return addonConfigs.computeIfAbsent(
                addon.getId(),
                (addonId) -> new VoiceAddonConfig(addon, config.getAddons().getAddon(addon.getId()))
        );
    }

    public abstract String getServerIp();

    public abstract MinecraftClientLib getMinecraft();

    protected abstract PlayerVolumeAction createPlayerVolumeAction(@NotNull MinecraftClientLib minecraft);
}
