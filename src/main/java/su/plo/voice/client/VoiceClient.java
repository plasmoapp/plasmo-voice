package su.plo.voice.client;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import su.plo.voice.data.DataEntity;
import su.plo.voice.gui.VoiceHud;
import su.plo.voice.gui.settings.VoiceNotAvailableScreen;
import su.plo.voice.gui.settings.VoiceSettingsScreen;
import su.plo.voice.socket.SocketClientUDP;
import su.plo.voice.socket.SocketClientUDPQueue;
import su.plo.voice.sound.AudioNatives;
import su.plo.voice.sound.Recorder;
import su.plo.voice.sound.ThreadSoundQueue;

import java.io.*;
import java.util.*;

@Environment(EnvType.CLIENT)
public class VoiceClient implements ClientModInitializer {
    public static final Identifier MICS = new Identifier("plasmo_voice", "textures/gui/mics.png");
    public static final Identifier SPEAKER_ICON = new Identifier("plasmo_voice", "textures/gui/speaker.png");
    public static final Identifier SPEAKER_PRIORITY = new Identifier("plasmo_voice", "textures/gui/speaker_priority.png");
    public static final Identifier SPEAKER_MUTED = new Identifier("plasmo_voice", "textures/gui/speaker_muted.png");
    public static final Identifier SPEAKER_WARNING = new Identifier("plasmo_voice", "textures/gui/speaker_warning.png");

    public static VoiceClientConfig config;
    public static VoiceServerConfig serverConfig;
    public static HashSet<UUID> clientMutedClients = new HashSet<>();

    public static SocketClientUDP socketUDP;
    static {
        AudioNatives.ensureOpus();
    }
    public static Recorder recorder = new Recorder();

    public static boolean muted = false;
    public static boolean speaking = false;
    public static boolean speakingPriority = false;

    public static KeyBinding pushToTalk;
    public static KeyBinding priorityPushToTalk;
    public static KeyBinding menuKey;
    public static KeyBinding muteKey;
    public static Set<Integer> mouseKeyPressed = new HashSet<>();

    public final static Gson gson = new Gson();
    public static final Logger LOGGER = LogManager.getLogger("Plasmo Voice");

    @Override
    public void onInitializeClient() {
        config = new VoiceClientConfig();
        config.load();

        pushToTalk = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.plasmo_voice.ptt",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_LEFT_ALT,
                        "key.plasmo_voice")
        );

        priorityPushToTalk = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.plasmo_voice.priority_ptt",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_UNKNOWN,
                        "key.plasmo_voice")
        );

        menuKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.plasmo_voice.settings",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        "key.plasmo_voice")
        );

        muteKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.plasmo_voice.mute",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_M,
                        "key.plasmo_voice")
        );

        readDataFile();
        saveDataFile();

        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("vc")
                .then(ClientCommandManager.literal("priority-distance")
                        .executes(ctx -> {
                            ctx.getSource().getPlayer().sendMessage(new TranslatableText("commands.plasmo_voice.priority_distance_set", VoiceClient.serverConfig.priorityDistance), false);
                            return 1;
                        })
                        .then(ClientCommandManager.argument("distance", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int distance = IntegerArgumentType.getInteger(ctx, "distance");
                                    if(distance <= VoiceClient.serverConfig.maxDistance) {
                                        ctx.getSource().getPlayer().sendMessage(new TranslatableText("commands.plasmo_voice.min_priority_distance", VoiceClient.serverConfig.maxDistance), false);
                                        return 1;
                                    }

                                    if(distance > VoiceClient.serverConfig.maxPriorityDistance) {
                                        ctx.getSource().getPlayer().sendMessage(new TranslatableText("commands.plasmo_voice.max_priority_distance", VoiceClient.serverConfig.maxPriorityDistance), false);
                                        return 1;
                                    }

                                    VoiceClientServerConfig serverConfig;
                                    if(VoiceClient.config.servers.containsKey(VoiceClient.serverConfig.ip)) {
                                        serverConfig = VoiceClient.config.servers.get(VoiceClient.serverConfig.ip);
                                    } else {
                                        serverConfig = new VoiceClientServerConfig();
                                    }

                                    serverConfig.priorityDistance = (short) distance;
                                    VoiceClient.serverConfig.priorityDistance = (short) distance;
                                    VoiceClient.config.servers.put(VoiceClient.serverConfig.ip, serverConfig);
                                    VoiceClient.config.save();
                                    ctx.getSource().getPlayer().sendMessage(new TranslatableText("commands.plasmo_voice.priority_distance_set", distance), false);
                                    return 1;
                                }))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            final PlayerEntity player = client.player;
            if(player == null) {
                return;
            }

            if(socketUDP == null || serverConfig == null) {
                // Voice not available
                if(menuKey.wasPressed()) {
                    MinecraftClient.getInstance().openScreen(new VoiceNotAvailableScreen(new TranslatableText("gui.plasmo_voice.not_available"), client));
                }

                return;
            }

            if(muteKey.wasPressed()) {
                muted = !muted;
            }

            if(menuKey.wasPressed()) {
                if(MinecraftClient.getInstance().currentScreen instanceof VoiceSettingsScreen) {
                    MinecraftClient.getInstance().openScreen(null);
                } else {
                    MinecraftClient.getInstance().openScreen(new VoiceSettingsScreen());
                }
            }
        });

        VoiceHud voiceHud = new VoiceHud();

        HudRenderCallback.EVENT.register((__, ___) -> {
            voiceHud.render();
        });
    }

    public static void disconnect() {
        if(VoiceClient.socketUDP != null) {
            VoiceClient.socketUDP.close();
        }

        VoiceClient.recorder.running = false;
        VoiceClient.serverConfig = null;

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