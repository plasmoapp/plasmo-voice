package su.plo.voice;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.command.CommandSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.launch.MixinBootstrap;
import su.plo.voice.data.DataEntity;
import su.plo.voice.event.ClientInputEvent;
import su.plo.voice.event.ClientNetworkEvent;
import su.plo.voice.event.RenderEvent;
import su.plo.voice.event.VoiceChatCommandEvent;
import su.plo.voice.socket.SocketClientUDP;
import su.plo.voice.socket.SocketClientUDPQueue;
import su.plo.voice.sound.AudioNatives;
import su.plo.voice.sound.Recorder;
import su.plo.voice.sound.ThreadSoundQueue;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Mod("plasmovoice")
public class Voice {
    public static final ResourceLocation MICS = new ResourceLocation("plasmo_voice", "textures/gui/mics.png");
    public static final ResourceLocation SPEAKER_ICON = new ResourceLocation("plasmo_voice", "textures/gui/speaker.png");
    public static final ResourceLocation SPEAKER_PRIORITY = new ResourceLocation("plasmo_voice", "textures/gui/speaker_priority.png");
    public static final ResourceLocation SPEAKER_MUTED = new ResourceLocation("plasmo_voice", "textures/gui/speaker_muted.png");
    public static final ResourceLocation SPEAKER_WARNING = new ResourceLocation("plasmo_voice", "textures/gui/speaker_warning.png");

    public final static Gson gson = new Gson();
    public static final Logger LOGGER = LogManager.getLogger();

    public static final ResourceLocation PLASMO_VOICE = new ResourceLocation("plasmo:voice");
    public static final String version = "1.0.0";

    public static VoiceClientConfig config;
    public static VoiceServerConfig serverConfig;
    public static HashSet<UUID> clientMutedClients = new HashSet<>();

    public static SocketClientUDP socketUDP;

    static {
        AudioNatives.ensureOpus();
    }

    public final static Recorder recorder = new Recorder();

    public static boolean muted = false;
    public static boolean speaking = false;
    public static boolean speakingPriority = false;

    public static KeyBinding pushToTalk;
    public static KeyBinding priorityPushToTalk;
    public static KeyBinding menuKey;
    public static KeyBinding muteKey;

    public Voice() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(new ClientInputEvent());
        MinecraftForge.EVENT_BUS.register(new ClientNetworkEvent());
        MinecraftForge.EVENT_BUS.register(new RenderEvent());
        MinecraftForge.EVENT_BUS.register(new VoiceChatCommandEvent());
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        config = new VoiceClientConfig();
        config.load();

        pushToTalk = new KeyBinding("key.plasmo_voice.ptt", GLFW.GLFW_KEY_LEFT_ALT, "key.plasmo_voice");
        ClientRegistry.registerKeyBinding(pushToTalk);

        priorityPushToTalk = new KeyBinding("key.plasmo_voice.priority_ptt", GLFW.GLFW_KEY_UNKNOWN, "key.plasmo_voice");
        ClientRegistry.registerKeyBinding(priorityPushToTalk);

        menuKey = new KeyBinding("key.plasmo_voice.settings", GLFW.GLFW_KEY_V, "key.plasmo_voice");
        ClientRegistry.registerKeyBinding(menuKey);

        muteKey = new KeyBinding("key.plasmo_voice.mute", GLFW.GLFW_KEY_M, "key.plasmo_voice");
        ClientRegistry.registerKeyBinding(muteKey);

        readDataFile();
        saveDataFile();
    }

    public static void disconnect() {
        if(socketUDP != null) {
            socketUDP.close();
        }

        recorder.running = false;
        serverConfig = null;

        SocketClientUDPQueue.talking.clear();
        SocketClientUDPQueue.audioChannels.values().forEach(ThreadSoundQueue::closeAndKill);
        SocketClientUDPQueue.audioChannels.clear();
    }

    public static void readDataFile() {
        File dataFile = new File("config/PlasmoVoice/data.json");
        if(dataFile.exists()) {
            try {
                JsonReader reader = new JsonReader(new FileReader(dataFile));
                try {
                    DataEntity data = gson.fromJson(reader, DataEntity.class);
                    if(data != null) {
                        clientMutedClients.addAll(data.mutedClients);
                    }
                } catch (JsonSyntaxException j) {
                    dataFile.delete();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveDataFile() {
        File configDir = new File("config/PlasmoVoice");
        configDir.mkdirs();

        try {
            try(Writer w = new FileWriter("config/PlasmoVoice/data.json")) {
                List<UUID> list = new ArrayList<>(clientMutedClients);
                w.write(gson.toJson(new DataEntity(list)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
