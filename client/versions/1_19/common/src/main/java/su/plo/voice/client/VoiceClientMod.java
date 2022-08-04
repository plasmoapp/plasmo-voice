package su.plo.voice.client;

import io.netty.channel.local.LocalAddress;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import su.plo.voice.api.client.audio.device.DeviceFactoryManager;
import su.plo.voice.client.audio.device.AlInputDeviceFactory;
import su.plo.voice.client.audio.device.AlOutputDeviceFactory;
import su.plo.voice.client.audio.device.JavaxInputDeviceFactory;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class VoiceClientMod extends BaseVoiceClient {

    public static final ResourceLocation CHANNEL = new ResourceLocation(CHANNEL_STRING);

    protected final String modId = "plasmovoice";
    protected final Minecraft minecraft = Minecraft.getInstance();

    protected VoiceClientMod() {
        DeviceFactoryManager factoryManager = getDeviceFactoryManager();

        // OpenAL in&out
        factoryManager.registerDeviceFactory(new AlOutputDeviceFactory(this));
        factoryManager.registerDeviceFactory(new AlInputDeviceFactory(this));

        // JavaX input
        factoryManager.registerDeviceFactory(new JavaxInputDeviceFactory(this));
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
}
