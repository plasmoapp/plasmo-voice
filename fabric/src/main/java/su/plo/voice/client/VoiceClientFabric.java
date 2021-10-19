package su.plo.voice.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TranslatableComponent;
import org.lwjgl.glfw.GLFW;
import su.plo.voice.client.config.ClientConfig;
import su.plo.voice.client.gui.VoiceHud;
import su.plo.voice.client.gui.VoiceNotAvailableScreen;
import su.plo.voice.client.gui.VoiceSettingsScreen;
import su.plo.voice.client.network.ClientNetworkHandlerFabric;

@Environment(EnvType.CLIENT)
public class VoiceClientFabric extends VoiceClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        super.initialize();

        menuKey = KeyBindingHelper.registerKeyBinding(
                new KeyMapping("key.plasmo_voice.settings",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_V,
                        "Plasmo Voice")
        );

        ClientNetworkHandlerFabric network = new ClientNetworkHandlerFabric();
        ClientPlayNetworking.registerGlobalReceiver(PLASMO_VOICE, network::handle);

        ClientCommandManager.DISPATCHER.register(ClientCommandManager.literal("vc")
                .then(ClientCommandManager.literal("muteall")
                        .executes(ctx -> {
                            if (getClientConfig().whitelist.get()) {
                                ctx.getSource().getPlayer().sendMessage(new TranslatableComponent("commands.plasmo_voice.whitelist_off"), NIL_UUID);
                            } else {
                                ctx.getSource().getPlayer().sendMessage(new TranslatableComponent("commands.plasmo_voice.whitelist_on"), NIL_UUID);
                            }

                            getClientConfig().whitelist.invert();
                            getClientConfig().save();
                            return 1;
                        }))
                .then(ClientCommandManager.literal("priority-distance")
                        .executes(ctx -> {
                            ctx.getSource().getPlayer().sendMessage(
                                    new TranslatableComponent("commands.plasmo_voice.priority_distance_set",
                                            getServerConfig().getPriorityDistance()
                                    ), NIL_UUID);
                            return 1;
                        })
                        .then(ClientCommandManager.argument("distance", IntegerArgumentType.integer())
                                .executes(ctx -> {
                                    int distance = IntegerArgumentType.getInteger(ctx, "distance");
                                    if (distance <= getServerConfig().getMaxDistance()) {
                                        ctx.getSource().getPlayer().sendMessage(
                                                new TranslatableComponent("commands.plasmo_voice.min_priority_distance",
                                                        getServerConfig().getMaxDistance()
                                                ), NIL_UUID);
                                        return 1;
                                    }

                                    if (distance > getServerConfig().getMaxPriorityDistance()) {
                                        ctx.getSource().getPlayer().sendMessage(
                                                new TranslatableComponent("commands.plasmo_voice.max_priority_distance",
                                                        getServerConfig().getMaxPriorityDistance()
                                                ), NIL_UUID);
                                        return 1;
                                    }

                                    ClientConfig.ServerConfig serverConfig;
                                    if (getClientConfig().getServers()
                                            .containsKey(getServerConfig().getIp())) {
                                        serverConfig = getClientConfig().getServers()
                                                .get(getServerConfig().getIp());
                                    } else {
                                        serverConfig = new ClientConfig.ServerConfig();
                                        serverConfig.distance.setDefault((int) getServerConfig().getDefaultDistance());
                                        getClientConfig().getServers().put(getServerConfig().getIp(), serverConfig);
                                    }

                                    serverConfig.priorityDistance.set(distance);
                                    getServerConfig().setPriorityDistance((short) distance);
                                    getClientConfig().save();
                                    ctx.getSource().getPlayer().sendMessage(
                                            new TranslatableComponent("commands.plasmo_voice.priority_distance_set",
                                                    distance
                                            ), NIL_UUID);
                                    return 1;
                                }))));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            final LocalPlayer player = client.player;
            if (player == null) {
                return;
            }

            if (!isConnected()) {
                // Voice not available
                if (menuKey.consumeClick()) {
                    VoiceNotAvailableScreen screen = new VoiceNotAvailableScreen();
                    if (socketUDP != null) {
                        if (socketUDP.ping.timedOut) {
                            screen.setConnecting();
                        } else if (!socketUDP.authorized) {
                            screen.setConnecting();
                        } else {
                            screen.setCannotConnect();
                        }
                    }
                    client.setScreen(screen);
                }

                return;
            }

            if (menuKey.consumeClick()) {
                if (client.screen instanceof VoiceSettingsScreen) {
                    client.setScreen(null);
                } else {
                    client.setScreen(new VoiceSettingsScreen());
                }
            }
        });

        VoiceHud voiceHud = new VoiceHud();
        HudRenderCallback.EVENT.register((__, ___) -> {
            voiceHud.render();
        });
    }

    @Override
    public String getVersion() {
        ModContainer modContainer = FabricLoader.getInstance()
                .getModContainer("plasmo_voice")
                .orElse(null);
        if (modContainer == null) {
            return "how can it be null?";
        }
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }
}
