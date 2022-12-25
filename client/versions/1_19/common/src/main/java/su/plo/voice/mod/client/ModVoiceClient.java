package su.plo.voice.mod.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.netty.channel.local.LocalAddress;
import lombok.Getter;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import su.plo.lib.api.client.MinecraftClientLib;
import su.plo.lib.mod.client.ModClientLib;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.api.client.connection.ServerConnection;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.client.BaseVoiceClient;
import su.plo.voice.client.event.key.KeyPressedEvent;
import su.plo.voice.client.gui.PlayerVolumeAction;
import su.plo.voice.mod.client.audio.device.AlInputDeviceFactory;
import su.plo.voice.mod.client.audio.device.AlOutputDeviceFactory;
import su.plo.voice.mod.client.audio.source.ModClientSourceManager;
import su.plo.voice.mod.client.connection.ModClientChannelHandler;
import su.plo.voice.mod.client.render.ModEntityRenderer;
import su.plo.voice.mod.client.render.ModHudRenderer;
import su.plo.voice.mod.client.render.ModLevelRenderer;
import su.plo.voice.mod.client.render.ModPlayerVolumeAction;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Optional;

public abstract class ModVoiceClient<T extends ModClientChannelHandler> extends BaseVoiceClient {

    public static final ResourceLocation CHANNEL = new ResourceLocation(CHANNEL_STRING);
    // static instance is used for access from mixins
    public static ModVoiceClient INSTANCE;

    protected final String modId = "plasmovoice";
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final ModClientLib minecraftLib = new ModClientLib();

    protected final KeyMapping menuKey = new KeyMapping(
            "PV settings",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "Plasmo Voice"
    );

    @Getter
    protected final ModHudRenderer hudRenderer;
    @Getter
    protected final ModLevelRenderer levelRenderer;
    @Getter
    protected final ModEntityRenderer entityRenderer;

    @Getter
    protected ClientSourceManager sourceManager;

    protected T handler;

    protected ModVoiceClient() {
        DeviceFactoryManager factoryManager = getDeviceFactoryManager();

        // OpenAL input
        factoryManager.registerDeviceFactory(new AlOutputDeviceFactory(this));
        factoryManager.registerDeviceFactory(new AlInputDeviceFactory(this));

        this.hudRenderer = new ModHudRenderer(minecraftLib, this);
        this.levelRenderer = new ModLevelRenderer(minecraftLib, this);
        this.entityRenderer = new ModEntityRenderer(minecraftLib, this);

        INSTANCE = this;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        this.sourceManager = new ModClientSourceManager(minecraftLib, this, config);
        eventBus.register(this, sourceManager);

        this.handler = createChannelHandler();
    }

    @Override
    protected void onServerDisconnect() {
        super.onServerDisconnect();
        handler.close();
    }

    @EventSubscribe
    public void onKeyPressed(@NotNull KeyPressedEvent event) {
        if (minecraftLib.getClientPlayer().isEmpty()) return;
        if (menuKey.consumeClick()) openSettings();
    }

    @Override
    public @NotNull File getConfigFolder() {
        return new File("config/" + modId);
    }

    @Override
    protected File modsFolder() {
        return new File("mods");
    }

    @Override
    public String getServerIp() {
        if (minecraft.getConnection() == null) throw new IllegalStateException("Not connected to any server");
        Connection connection = minecraft.getConnection().getConnection();
        SocketAddress socketAddress = connection.getRemoteAddress();

        if (!(socketAddress instanceof InetSocketAddress) && !(socketAddress instanceof LocalAddress)) {
            throw new IllegalStateException("Not connected to any server");
        }

        String serverIp = "127.0.0.1";
        if (connection.getRemoteAddress() instanceof InetSocketAddress address) {
            InetAddress inetAddress = address.getAddress();
            String[] ipSplit = inetAddress.toString().split("/");

            serverIp = ipSplit[0];
            if (ipSplit.length > 1) {
                serverIp = ipSplit[1];
            }
        }

        return serverIp;
    }

    @Override
    public MinecraftClientLib getMinecraft() {
        return minecraftLib;
    }

    @Override
    public Optional<ServerConnection> getServerConnection() {
        return handler.getConnection();
    }

    @Override
    protected PlayerVolumeAction createPlayerVolumeAction(@NotNull MinecraftClientLib minecraft) {
        return new ModPlayerVolumeAction(minecraft, this, config);
    }

    protected abstract T createChannelHandler();
}
