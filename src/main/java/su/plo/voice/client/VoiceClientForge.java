package su.plo.voice.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import org.lwjgl.glfw.GLFW;
import su.plo.voice.client.config.VoiceServerConfig;
import su.plo.voice.client.event.ClientInputEvent;
import su.plo.voice.client.event.ClientNetworkEvent;
import su.plo.voice.client.event.RenderEvent;
import su.plo.voice.client.event.VoiceChatCommandEvent;
import su.plo.voice.client.network.VoiceNetworkPacket;
import su.plo.voice.client.socket.SocketClientUDP;
import su.plo.voice.client.socket.SocketClientUDPQueue;
import su.plo.voice.client.sound.AbstractSoundQueue;
import su.plo.voice.client.sound.openal.CustomSoundEngine;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.tcp.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Mod("plasmovoice")
public class VoiceClientForge extends VoiceClient {
    static {
        soundEngine = new CustomSoundEngine();
    }

    public VoiceClientForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(new ClientInputEvent());
        MinecraftForge.EVENT_BUS.register(new ClientNetworkEvent());
        MinecraftForge.EVENT_BUS.register(new RenderEvent());
        MinecraftForge.EVENT_BUS.register(new VoiceChatCommandEvent());
    }

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            PLASMO_VOICE,
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private void clientSetup(final FMLClientSetupEvent event) {
        super.initialize();

        menuKey = new KeyMapping("key.plasmo_voice.settings", GLFW.GLFW_KEY_V, "key.plasmo_voice");
        ClientRegistry.registerKeyBinding(menuKey);

        CHANNEL.registerMessage(0, ClientsListPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientsListPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientsListPacket()), (msg, ctx) -> {
                    getServerConfig().getMuted().clear();
                    VoiceClientForge.getServerConfig().getClients().clear();

                    List<String> mutedList = new ArrayList<>();

                    for(MutedEntity muted : msg.getMuted()) {
                        mutedList.add(muted.uuid.toString());
                        VoiceClientForge.getServerConfig().getMuted().put(muted.uuid, muted);
                    }
                    VoiceClientForge.getServerConfig().getClients().addAll(msg.getClients());

                    VoiceClientForge.LOGGER.info("Clients: " + msg.getClients().toString());
                    VoiceClientForge.LOGGER.info("Muted clients: " + mutedList);
                    ctx.get().setPacketHandled(true);
                });

        CHANNEL.registerMessage(1, ClientMutedPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientMutedPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientMutedPacket()), (msg, ctx) -> {
                    VoiceClientForge.getServerConfig().getMuted().put(msg.getClient(), new MutedEntity(msg.getClient(), msg.getTo()));
                    AbstractSoundQueue queue = SocketClientUDPQueue.audioChannels.get(msg.getClient());
                    if(queue != null) {
                        queue.closeAndKill();
                        SocketClientUDPQueue.audioChannels.remove(msg.getClient());
                    }

                    VoiceClientForge.LOGGER.info(msg.getClient().toString() + " muted");
                    ctx.get().setPacketHandled(true);
                });

        CHANNEL.registerMessage(2, ClientUnmutedPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientUnmutedPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientUnmutedPacket()), (msg, ctx) -> {
                    VoiceClientForge.getServerConfig().getMuted().remove(msg.getClient());
                    VoiceClientForge.LOGGER.info(msg.getClient().toString() + " unmuted");
                    ctx.get().setPacketHandled(true);
                });

        CHANNEL.registerMessage(3, ClientConnectedPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientConnectedPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientConnectedPacket()), (msg, ctx) -> {
                    if(msg.getMuted() != null) {
                        MutedEntity muted = msg.getMuted();
                        VoiceClientForge.getServerConfig().getMuted().put(muted.uuid, muted);
                    }

                    VoiceClientForge.getServerConfig().getClients().add(msg.getClient());
                    ctx.get().setPacketHandled(true);
                });

        CHANNEL.registerMessage(4, ClientDisconnectedPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientDisconnectedPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientDisconnectedPacket()), (msg, ctx) -> {
                    VoiceClientForge.getServerConfig().getClients().remove(msg.getClient());
                    VoiceClientForge.getServerConfig().getMuted().remove(msg.getClient());
                    ctx.get().setPacketHandled(true);
                });

        CHANNEL.registerMessage(5, ConfigPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ConfigPacket) VoiceNetworkPacket.readFromBuf(buf, new ConfigPacket()), (msg, ctx) -> {
                    VoiceClientForge.getServerConfig().update(msg);

                    if(!VoiceClientForge.isConnected()) {
                        try {
                            VoiceClientForge.socketUDP = new SocketClientUDP();
                            VoiceClientForge.socketUDP.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    ctx.get().setPacketHandled(true);
                });

        CHANNEL.registerMessage(6, ServerConnectPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ServerConnectPacket) VoiceNetworkPacket.readFromBuf(buf, new ServerConnectPacket()), (msg, ctx) -> {
                    VoiceClientForge.disconnect();
                    VoiceClientForge.socketUDP = null;

                    String ip = msg.getIp();

                    InetSocketAddress addr = (InetSocketAddress) ctx.get().getNetworkManager().getRemoteAddress();
                    Inet4Address in4addr = (Inet4Address) addr.getAddress();
                    String[] ipSplit = in4addr.toString().split("/");

                    String serverIp = ipSplit[0];
                    if(ipSplit.length > 1) {
                        serverIp = ipSplit[1];
                    }

                    if(ip.equals("0.0.0.0")) {
                        ip = serverIp;
                    }

                    VoiceClientForge.LOGGER.info("Connecting to " + Minecraft.getInstance().getCurrentServer().ip);

                    VoiceClientForge.setServerConfig(new VoiceServerConfig(msg.getToken(), ip,
                            msg.getPort(), msg.hasPriority()));

                    CHANNEL.sendToServer(new ClientConnectPacket(msg.getToken(), VoiceClientForge.PROTOCOL_VERSION));
                    ctx.get().setPacketHandled(true);
                });

        CHANNEL.registerMessage(7, ClientConnectPacket.class, VoiceNetworkPacket::writeToBuf,
                buf -> (ClientConnectPacket) VoiceNetworkPacket.readFromBuf(buf, new ClientConnectPacket()), null);

        new Thread(() -> soundEngine.init(true)).start();
    }
}
