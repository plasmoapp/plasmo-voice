package su.plo.voice.client;

import io.netty.channel.local.LocalAddress;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import su.plo.lib.client.MinecraftClientLib;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.client.audio.device.AlInputDeviceFactory;
import su.plo.voice.client.audio.device.AlOutputDeviceFactory;
import su.plo.voice.client.audio.device.JavaxInputDeviceFactory;
import su.plo.voice.client.audio.source.ModClientSourceManager;
import su.plo.voice.client.render.ModEntityRenderer;
import su.plo.voice.client.render.ModHudRenderer;
import su.plo.voice.client.render.ModLevelRenderer;
import su.plo.voice.lib.client.ModClientLib;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class ModVoiceClient extends BaseVoiceClient {

    public static final ResourceLocation CHANNEL = new ResourceLocation(CHANNEL_STRING);
    // static instance is used for access from mixins
    public static ModVoiceClient INSTANCE;

    protected final String modId = "plasmovoice";
    protected final Minecraft minecraft = Minecraft.getInstance();
    protected final ModClientLib minecraftLib = new ModClientLib();

    @Getter
    protected final ModHudRenderer hudRenderer;
    @Getter
    protected final ModLevelRenderer levelRenderer;
    @Getter
    protected final ModEntityRenderer entityRenderer;

    @Getter
    protected ClientSourceManager sourceManager;

    protected ModVoiceClient() {
        DeviceFactoryManager factoryManager = getDeviceFactoryManager();

        // OpenAL in&out
        factoryManager.registerDeviceFactory(new AlOutputDeviceFactory(this));
        factoryManager.registerDeviceFactory(new AlInputDeviceFactory(this));

        // JavaX input
        factoryManager.registerDeviceFactory(new JavaxInputDeviceFactory(this));

        this.hudRenderer = new ModHudRenderer(minecraftLib, this);
        this.levelRenderer = new ModLevelRenderer(minecraftLib, this);
        this.entityRenderer = new ModEntityRenderer(minecraftLib, this);

        INSTANCE = this;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        this.sourceManager = new ModClientSourceManager(this, config);
        eventBus.register(this, sourceManager);
    }

    @Override
    protected InputStream getResource(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
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
        if (connection.getRemoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress addr = (InetSocketAddress) connection.getRemoteAddress();
            Inet4Address in4addr = (Inet4Address) addr.getAddress();
            String[] ipSplit = in4addr.toString().split("/");

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
}
