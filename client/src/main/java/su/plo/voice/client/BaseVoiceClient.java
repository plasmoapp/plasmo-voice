package su.plo.voice.client;

import com.google.common.collect.Maps;
import gg.essential.universal.UChat;
import gg.essential.universal.UScreen;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;
import su.plo.config.provider.ConfigurationProvider;
import su.plo.config.provider.toml.TomlConfiguration;
import su.plo.lib.api.chat.MinecraftTextClickEvent;
import su.plo.lib.api.chat.MinecraftTextComponent;
import su.plo.lib.api.chat.MinecraftTextHoverEvent;
import su.plo.lib.api.chat.MinecraftTextStyle;
import su.plo.lib.mod.client.MinecraftUtil;
import su.plo.lib.mod.client.gui.screen.ScreenWrapper;
import su.plo.lib.mod.client.render.RenderUtil;
import su.plo.voice.BaseVoice;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.addon.AddonScope;
import su.plo.voice.api.client.PlasmoVoiceClient;
import su.plo.voice.api.client.audio.capture.AudioCapture;
import su.plo.voice.api.client.audio.capture.ClientActivationManager;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.device.DeviceManager;
import su.plo.voice.api.client.audio.line.ClientSourceLineManager;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.api.client.config.addon.AddonConfig;
import su.plo.voice.api.client.config.keybind.KeyBindings;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.client.connection.ServerInfo;
import su.plo.voice.api.client.connection.UdpClientManager;
import su.plo.voice.api.client.event.VoiceClientInitializedEvent;
import su.plo.voice.api.client.event.VoiceClientShutdownEvent;
import su.plo.voice.api.client.event.socket.UdpClientClosedEvent;
import su.plo.voice.api.client.event.socket.UdpClientConnectedEvent;
import su.plo.voice.api.client.render.DistanceVisualizer;
import su.plo.voice.api.client.socket.UdpClient;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.audio.capture.VoiceAudioCapture;
import su.plo.voice.client.audio.capture.VoiceClientActivationManager;
import su.plo.voice.client.audio.device.JavaxInputDeviceFactory;
import su.plo.voice.client.audio.device.VoiceDeviceFactoryManager;
import su.plo.voice.client.audio.device.VoiceDeviceManager;
import su.plo.voice.client.audio.line.VoiceClientSourceLineManager;
import su.plo.voice.client.audio.source.VoiceClientSourceManager;
import su.plo.lib.mod.client.chat.ClientLanguageSupplier;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.addon.VoiceAddonConfig;
import su.plo.voice.client.config.keybind.HotkeyActions;
import su.plo.voice.client.connection.VoiceUdpClientManager;
import su.plo.voice.client.gui.PlayerVolumeAction;
import su.plo.voice.client.gui.settings.VoiceNotAvailableScreen;
import su.plo.voice.client.gui.settings.VoiceSettingsScreen;
import su.plo.voice.client.render.voice.HudIconRenderer;
import su.plo.voice.client.render.voice.OverlayRenderer;
import su.plo.voice.client.render.voice.SourceIconRenderer;
import su.plo.voice.client.render.voice.VoiceDistanceVisualizer;
import su.plo.voice.util.version.ModrinthVersion;

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
    private ClientSourceManager sourceManager;
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

    @EventSubscribe
    public void onUdpConnected(@NotNull UdpClientConnectedEvent event) {
        try {
            ModrinthVersion.checkForUpdates(getVersion(), MinecraftUtil.getVersion(), getLoader())
                    .ifPresent(version -> {
                        UChat.chat(RenderUtil.getTextConverter().convert(
                                MinecraftTextComponent.translatable(
                                        "message.plasmovoice.update_available",
                                        version.version(),
                                        MinecraftTextComponent.translatable("message.plasmovoice.update_available.click")
                                                .withStyle(MinecraftTextStyle.YELLOW)
                                                .clickEvent(MinecraftTextClickEvent.openUrl(version.downloadLink()))
                                                .hoverEvent(MinecraftTextHoverEvent.showText(MinecraftTextComponent.translatable(
                                                        "message.plasmovoice.update_available.hover",
                                                        version.downloadLink()
                                                )))
                                )
                        ));
                    });
        } catch (Exception e) {
            logger.warn("Failed to check for updates", e);
        }
    }

    // todo: why is this here?
    public void openSettings() {
        Optional<ScreenWrapper> wrappedScreen = ScreenWrapper.getCurrentWrappedScreen();
        if (wrappedScreen.map(screen -> screen.getScreen() instanceof VoiceSettingsScreen)
                .orElse(false)
        ) {
            ScreenWrapper.openScreen(null);
            return;
        }

        if (!udpClientManager.isConnected()) {
            openNotAvailable();
            return;
        }

        if (settingsScreen == null) {
            this.settingsScreen = new VoiceSettingsScreen(
                    this,
                    config
            );
        }
        ScreenWrapper.openScreen(settingsScreen);
    }

    // todo: why is this here?
    public void openNotAvailable() {
        VoiceNotAvailableScreen notAvailableScreen = new VoiceNotAvailableScreen(this);

        Optional<UdpClient> udpClient = udpClientManager.getClient();
        if (udpClient.isPresent()) {
            if (udpClient.get().isClosed()) {
                notAvailableScreen.setCannotConnect();
            } else {
                notAvailableScreen.setConnecting();
            }
        }

        ScreenWrapper.openScreen(notAvailableScreen);
    }

    @Override
    protected void onInitialize() {
        loadAddons();
        super.onInitialize();

        loadConfig();

        this.distanceVisualizer = new VoiceDistanceVisualizer(this, config);

        this.deviceManager = new VoiceDeviceManager(this, config);
        this.sourceLineManager = new VoiceClientSourceLineManager(config);
        this.activationManager = new VoiceClientActivationManager(this, config);
        this.sourceManager = new VoiceClientSourceManager(this, config);
        this.audioCapture = new VoiceAudioCapture(this, config);

        eventBus.register(this, sourceManager);

        // hotkey actions
        new HotkeyActions(getKeyBindings(), config).register();
        PlayerVolumeAction volumeAction = new PlayerVolumeAction(this, config);
        eventBus.register(this, volumeAction);

        // render
        eventBus.register(this, distanceVisualizer);
        eventBus.register(this, new HudIconRenderer(this, config));
        eventBus.register(this, new SourceIconRenderer(this, config, volumeAction));
        eventBus.register(this, new OverlayRenderer(this, config));

        getEventBus().call(new VoiceClientInitializedEvent(this));
    }

    @Override
    protected void onShutdown() {
        logger.info("Shutting down");

        if (config != null) config.save(false);

        eventBus.unregister(this);

        super.onShutdown();

        getEventBus().call(new VoiceClientShutdownEvent(this));
    }

    protected void onServerDisconnect() {
        udpClientManager.removeClient(UdpClientClosedEvent.Reason.DISCONNECT);
        getServerConnection().ifPresent(ServerConnection::close);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    protected AddonScope getScope() {
        return AddonScope.CLIENT;
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

    protected ClientLanguageSupplier createLanguageSupplier() {
        return () -> getServerConnection().map(ServerConnection::getLanguage);
    }

    private void loadConfig() {
        try {
            File configFile = new File(getConfigFolder(), "client.toml");

            this.config = toml.load(ClientConfig.class, configFile, false);
            toml.save(ClientConfig.class, config, configFile);

            config.setConfigFile(configFile);
            config.setAsyncExecutor(backgroundExecutor);

            eventBus.register(this, config.getKeyBindings());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load the config", e);
        }

        if (config.getDebug().value() || System.getProperty("plasmovoice.debug") != null) {
            Configurator.setLevel(logger.getName(), Level.DEBUG);
        } else {
            Configurator.setLevel(logger.getName(), Level.INFO);
        }
    }
}
