package su.plo.voice.client;

import io.netty.channel.local.LocalAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.api.client.audio.source.ClientSourceManager;
import su.plo.voice.client.audio.device.AlInputDeviceFactory;
import su.plo.voice.client.audio.device.AlOutputDeviceFactory;
import su.plo.voice.client.audio.device.JavaxInputDeviceFactory;
import su.plo.voice.client.audio.source.ModClientSourceManager;

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

    protected ClientSourceManager sources;

    protected ModVoiceClient() {
        DeviceFactoryManager factoryManager = getDeviceFactoryManager();

        // OpenAL in&out
        factoryManager.registerDeviceFactory(new AlOutputDeviceFactory(this));
        factoryManager.registerDeviceFactory(new AlInputDeviceFactory(this));

        // JavaX input
        factoryManager.registerDeviceFactory(new JavaxInputDeviceFactory(this));

        INSTANCE = this;
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        this.sources = new ModClientSourceManager(this, config);
        eventBus.register(this, sources);
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();
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
    public @NotNull ClientSourceManager getSourceManager() {
        return sources;
    }
}
