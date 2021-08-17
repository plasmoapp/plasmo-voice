package su.plo.voice.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import su.plo.voice.gui.VoiceHud;
import su.plo.voice.gui.settings.VoiceNotAvailableScreen;
import su.plo.voice.gui.settings.VoiceSettingsScreen;
import su.plo.voice.socket.SocketClientUDP;
import su.plo.voice.socket.SocketClientUDPQueue;
import su.plo.voice.sound.Recorder;
import su.plo.voice.sound.ThreadSoundQueue;

import java.util.HashSet;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class VoiceClient implements ClientModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("Plasmo Voice");

    @Getter
    private static VoiceClientConfig clientConfig;
    @Setter
    @Getter
    private static VoiceServerConfig serverConfig;

    // ???
    public static SocketClientUDP socketUDP;
    public final static Recorder recorder = new Recorder();

    @Setter
    @Getter
    private static boolean muted = false;
    @Setter
    @Getter
    private static boolean speaking = false;
    @Setter
    @Getter
    private static boolean speakingPriority = false;

    // key bindings
    public static KeyBinding pushToTalk;
    public static KeyBinding priorityPushToTalk;
    public static KeyBinding menuKey;
    public static KeyBinding muteKey;
    public static KeyBinding volumeKey;
    public static Set<Integer> mouseKeyPressed = new HashSet<>();

    // icons
    public static final Identifier MICS = new Identifier("plasmo_voice", "textures/gui/mics.png");
    public static final Identifier SPEAKER_ICON = new Identifier("plasmo_voice", "textures/gui/speaker.png");
    public static final Identifier SPEAKER_PRIORITY = new Identifier("plasmo_voice", "textures/gui/speaker_priority.png");
    public static final Identifier SPEAKER_MUTED = new Identifier("plasmo_voice", "textures/gui/speaker_muted.png");
    public static final Identifier SPEAKER_WARNING = new Identifier("plasmo_voice", "textures/gui/speaker_warning.png");

    @Override
    public void onInitializeClient() {
        clientConfig = VoiceClientConfig.read();

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

        volumeKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.plasmo_voice.volume",
                        InputUtil.Type.KEYSYM,
                        InputUtil.UNKNOWN_KEY.getCode(),
                        "key.plasmo_voice")
        );

        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("vc")
                .then(ClientCommandManager.literal("muteall")
                        .executes(ctx -> {
                            if (VoiceClient.getClientConfig().isWhitelist()) {
                                ctx.getSource().getPlayer().sendMessage(new TranslatableText("commands.plasmo_voice.whitelist_off"), false);
                            } else {
                                ctx.getSource().getPlayer().sendMessage(new TranslatableText("commands.plasmo_voice.whitelist_on"), false);
                            }

                            VoiceClient.getClientConfig().setWhitelist(!VoiceClient.getClientConfig().isWhitelist());
                            VoiceClient.getClientConfig().save();
                            return 1;
                        }))
                .then(ClientCommandManager.literal("priority-distance")
                        .executes(ctx -> {
                            ctx.getSource().getPlayer().sendMessage(
                                    new TranslatableText("commands.plasmo_voice.priority_distance_set",
                                            VoiceClient.getServerConfig().getPriorityDistance()
                                    ), false);
                            return 1;
                        })
                        .then(ClientCommandManager.argument("distance", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int distance = IntegerArgumentType.getInteger(ctx, "distance");
                                    if (distance <= VoiceClient.getServerConfig().getMaxDistance()) {
                                        ctx.getSource().getPlayer().sendMessage(
                                                new TranslatableText("commands.plasmo_voice.min_priority_distance",
                                                        VoiceClient.getServerConfig().getMaxDistance()
                                                ), false);
                                        return 1;
                                    }

                                    if (distance > VoiceClient.getServerConfig().getMaxPriorityDistance()) {
                                        ctx.getSource().getPlayer().sendMessage(
                                                new TranslatableText("commands.plasmo_voice.max_priority_distance",
                                                        VoiceClient.getServerConfig().getMaxPriorityDistance()
                                                ), false);
                                        return 1;
                                    }

                                    VoiceClientServerConfig serverConfig;
                                    if (VoiceClient.getClientConfig().getServers()
                                            .containsKey(VoiceClient.getServerConfig().getIp())) {
                                        serverConfig = VoiceClient.getClientConfig().getServers()
                                                .get(VoiceClient.getServerConfig().getIp());
                                    } else {
                                        serverConfig = new VoiceClientServerConfig();
                                    }

                                    serverConfig.setPriorityDistance((short) distance);
                                    VoiceClient.getServerConfig().setPriorityDistance((short) distance);
                                    VoiceClient.getClientConfig().getServers().put(VoiceClient.getServerConfig().getIp(), serverConfig);
                                    VoiceClient.getClientConfig().save();
                                    ctx.getSource().getPlayer().sendMessage(
                                            new TranslatableText("commands.plasmo_voice.priority_distance_set",
                                                    distance
                                            ), false);
                                    return 1;
                                }))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            final PlayerEntity player = client.player;
            if (player == null) {
                return;
            }

            if (!VoiceClient.isConnected()) {
                // Voice not available
                if (menuKey.wasPressed()) {
                    MinecraftClient.getInstance().setScreen(new VoiceNotAvailableScreen(
                            new TranslatableText("gui.plasmo_voice.not_available"), client)
                    );
                }

                return;
            }

            if (muteKey.wasPressed()) {
                muted = !muted;
            }

            if (menuKey.wasPressed()) {
                if (MinecraftClient.getInstance().currentScreen instanceof VoiceSettingsScreen) {
                    MinecraftClient.getInstance().setScreen(null);
                } else {
                    MinecraftClient.getInstance().setScreen(new VoiceSettingsScreen());
                }
            }
        });

        VoiceHud voiceHud = new VoiceHud();
        HudRenderCallback.EVENT.register((__, ___) -> {
            voiceHud.render();
        });
    }

    public static void disconnect() {
        if (VoiceClient.socketUDP != null) {
            VoiceClient.socketUDP.close();
        }

        VoiceClient.recorder.setRunning(false);
        VoiceClient.serverConfig = null;

        SocketClientUDPQueue.talking.clear();
        SocketClientUDPQueue.audioChannels.values().forEach(ThreadSoundQueue::closeAndKill);
        SocketClientUDPQueue.audioChannels.clear();
    }

    public static boolean isConnected() {
        if (socketUDP == null) {
            return false;
        }

        if (serverConfig == null) {
            return false;
        }

        return socketUDP.authorized;
    }
}