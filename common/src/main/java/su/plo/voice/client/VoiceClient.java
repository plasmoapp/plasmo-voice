package su.plo.voice.client;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.config.ServerSettings;
import su.plo.voice.client.gui.PlayerVolumeHandler;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.client.socket.SocketConnection;
import su.plo.voice.client.sound.AbstractSoundQueue;
import su.plo.voice.client.sound.Recorder;
import su.plo.voice.client.sound.openal.CustomSoundEngine;

import java.util.UUID;

public abstract class VoiceClient {
    @Getter
    private static VoiceClient instance;

    public static final ResourceLocation PLASMO_VOICE = new ResourceLocation("plasmo:voice");
    public static final String PROTOCOL_VERSION = "1.0.0";
    public static final Logger LOGGER = LogManager.getLogger("Plasmo Voice");
    public static final UUID NIL_UUID = new UUID(0, 0);

    @Getter
    private static ClientConfig clientConfig;
    @Getter
    private static ClientConfig.ConfigKeyBindings keyBindings;
    @Setter
    @Getter
    private static ServerSettings serverConfig;

    // ???
    public static SocketConnection socketUDP;
    public static Recorder recorder;
    @Getter
    public static final CustomSoundEngine soundEngine = new CustomSoundEngine();

    @Setter
    @Getter
    private static boolean speaking = false;
    @Setter
    @Getter
    private static boolean speakingPriority = false;

    // key bindings
    public static KeyMapping menuKey;

    // icons
    public static final ResourceLocation ICONS = new ResourceLocation("plasmo_voice", "textures/gui/icons.png");

    public void initialize() {
        instance = this;

        Minecraft minecraft = Minecraft.getInstance();
        clientConfig = ClientConfig.read();
        keyBindings = clientConfig.keyBindings;
        recorder = new Recorder();

        keyBindings.action.get().setOnPress(PlayerVolumeHandler::onButton);
        keyBindings.muteMicrophone.get().setOnPress(action -> {
            if (action == 1) {
                clientConfig.microphoneMuted.invert();
            }
        });
        keyBindings.increaseDistance.get().setOnPress(action -> {
            if (action == 1 && minecraft.player != null && VoiceClient.isConnected()) {
                ClientConfig.ServerConfig serverConfig;
                if (clientConfig.getServers().containsKey(VoiceClient.getServerConfig().getIp())) {

                    serverConfig = clientConfig.getServers().get(VoiceClient.getServerConfig().getIp());
                    int index = (getServerConfig().getDistances().indexOf(serverConfig.distance.get()) + 1) % getServerConfig().getDistances().size();
                    int value = getServerConfig().getDistances().get(index);
                    serverConfig.distance.set(value);
                    getServerConfig().setDistance((short) value);

                    minecraft.gui.setOverlayMessage(
                            new TranslatableComponent("message.plasmo_voice.distance_changed",
                                    value
                            ), false);
                }
            }
        });
        keyBindings.decreaseDistance.get().setOnPress(action -> {
            if (action == 1 && minecraft.player != null && VoiceClient.isConnected()) {
                ClientConfig.ServerConfig serverConfig;
                if (clientConfig.getServers().containsKey(VoiceClient.getServerConfig().getIp())) {
                    serverConfig = clientConfig.getServers().get(VoiceClient.getServerConfig().getIp());
                    int index = getServerConfig().getDistances().indexOf(serverConfig.distance.get()) - 1;
                    if (index < 0) {
                        index = getServerConfig().getDistances().size() - 1;
                    }
                    int value = getServerConfig().getDistances().get(index);
                    serverConfig.distance.set(value);
                    getServerConfig().setDistance((short) value);

                    minecraft.gui.setOverlayMessage(
                            new TranslatableComponent("message.plasmo_voice.distance_changed",
                                    value
                            ), false);
                }
            }
        });
        keyBindings.occlusion.get().setOnPress(action -> {
            if (action == 1 && minecraft.player != null && !soundEngine.isSoundPhysics() && VoiceClient.isConnected()) {
                clientConfig.occlusion.invert();

                if (clientConfig.occlusion.get()) {
                    minecraft.gui.setOverlayMessage(
                            new TranslatableComponent("message.plasmo_voice.occlusion_changed",
                                    new TranslatableComponent("gui.plasmo_voice.on")
                            ), false);
                } else {
                    minecraft.gui.setOverlayMessage(
                            new TranslatableComponent("message.plasmo_voice.occlusion_changed",
                                    new TranslatableComponent("gui.plasmo_voice.off")
                            ), false);
                }
            }
        });
    }

    public static void disconnect() {
        if (socketUDP != null) {
            socketUDP.close();
        }

        recorder.close(true);
        serverConfig = null;

        SocketClientUDPQueue.talking.clear();
        SocketClientUDPQueue.audioChannels.values().forEach(AbstractSoundQueue::closeAndKill);
        SocketClientUDPQueue.audioChannels.clear();
    }

    public static boolean isMicrophoneLoopback() {
        if (!(Minecraft.getInstance().screen instanceof VoiceSettingsScreen)) return false;

        VoiceSettingsScreen screen = (VoiceSettingsScreen) Minecraft.getInstance().screen;
        return screen.getSource() != null;
    }

    public static boolean isSettingsOpen() {
        return Minecraft.getInstance().screen instanceof VoiceSettingsScreen;
    }

    public static boolean isConnected() {
        return socketUDP != null &&
                serverConfig != null &&
                socketUDP.isConnected();
    }

    public abstract String getVersion();
}
