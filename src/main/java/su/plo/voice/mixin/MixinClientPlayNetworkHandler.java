package su.plo.voice.mixin;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.Voice;
import su.plo.voice.client.VoiceClient;
import su.plo.voice.client.VoiceServerConfig;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.*;
import su.plo.voice.socket.SocketClientUDP;
import su.plo.voice.socket.SocketClientUDPQueue;
import su.plo.voice.sound.ThreadSoundQueue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {
    @Shadow public abstract ClientConnection getConnection();

    @Shadow private MinecraftClient client;

    @Shadow @Final private ClientConnection connection;

    @Inject(method = "onGameJoin", at = @At("RETURN"))
    public void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBytes(("plasmo:voice").getBytes(StandardCharsets.US_ASCII));
        this.connection.send(new CustomPayloadC2SPacket(new Identifier("register"), buf));
    }

    @Inject(at = @At("HEAD"), method = "onCustomPayload", cancellable = true)
    public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo info) {
        Identifier identifier = packet.getChannel();
        if(identifier.equals(Voice.PLASMO_VOICE)) {
            info.cancel();

            PacketByteBuf packetByteBuf = null;
            try {
                packetByteBuf = packet.getData();
                byte[] data = new byte[packetByteBuf.readableBytes()];
                packetByteBuf.duplicate().readBytes(data);
                ByteArrayDataInput buf = ByteStreams.newDataInput(data);

                Packet pkt = PacketTCP.read(buf);
                if(pkt instanceof ServerConnectPacket) {
                    VoiceClient.disconnect();
                    VoiceClient.socketUDP = null;

                    ServerConnectPacket connect = (ServerConnectPacket) pkt;
                    String ip = connect.getIp();

                    InetSocketAddress addr = (InetSocketAddress) this.getConnection().getAddress();
                    Inet4Address in4addr = (Inet4Address) addr.getAddress();
                    String[] ipSplit = in4addr.toString().split("/");

                    String serverIp = ipSplit[0];
                    if(ipSplit.length > 1) {
                        serverIp = ipSplit[1];
                    }

                    if(ip.equals("0.0.0.0")) {
                        ip = serverIp;
                    }

                    VoiceClient.LOGGER.info("Connecting to " + client.getCurrentServerEntry().address);

                    VoiceClient.serverConfig = new VoiceServerConfig(connect.getToken(), ip,
                            connect.getPort(), connect.hasPriority());

                    this.connection.send(
                            new CustomPayloadC2SPacket(Voice.PLASMO_VOICE,
                                new PacketByteBuf(Unpooled.wrappedBuffer(PacketTCP.write(new ClientConnectPacket(connect.getToken(), Voice.version))))));
                } else if(pkt instanceof ConfigPacket) {
                    ConfigPacket config = (ConfigPacket) pkt;
                    VoiceClient.serverConfig.update(config);

                    if(!VoiceClient.connected()) {
                        VoiceClient.socketUDP = new SocketClientUDP();
                        VoiceClient.socketUDP.start();
                    }
                } else if(pkt instanceof ClientMutedPacket) {
                    ClientMutedPacket muted = (ClientMutedPacket) pkt;
                    VoiceClient.serverConfig.mutedClients.put(muted.getClient(), new MutedEntity(muted.getClient(), muted.getTo()));
                    ThreadSoundQueue queue = SocketClientUDPQueue.audioChannels.get(muted.getClient());
                    if(queue != null) {
                        queue.closeAndKill();
                        SocketClientUDPQueue.audioChannels.remove(muted.getClient());
                    }

                    VoiceClient.LOGGER.info(muted.getClient().toString() + " muted");
                } else if(pkt instanceof ClientUnmutedPacket) {
                    ClientUnmutedPacket unmuted = (ClientUnmutedPacket) pkt;
                    VoiceClient.serverConfig.mutedClients.remove(unmuted.getClient());

                    VoiceClient.LOGGER.info(unmuted.getClient().toString() + " unmuted");
                } else if(pkt instanceof ClientsListPacket) {
                    ClientsListPacket list = (ClientsListPacket) pkt;

                    VoiceClient.serverConfig.mutedClients.clear();
                    VoiceClient.serverConfig.clients.clear();

                    List<String> mutedList = new ArrayList<>();

                    for(MutedEntity muted : list.getMuted()) {
                        mutedList.add(muted.uuid.toString());
                        VoiceClient.serverConfig.mutedClients.put(muted.uuid, muted);
                    }
                    VoiceClient.serverConfig.clients.addAll(list.getClients());

                    VoiceClient.LOGGER.info("Clients: " + list.getClients().toString());
                    VoiceClient.LOGGER.info("Muted clients: " + mutedList);
                } else if(pkt instanceof ClientConnectedPacket) {
                    ClientConnectedPacket connected = (ClientConnectedPacket) pkt;
                    if(connected.getMuted() != null) {
                        MutedEntity muted = connected.getMuted();
                        VoiceClient.serverConfig.mutedClients.put(muted.uuid, muted);
                    }

                    VoiceClient.serverConfig.clients.add(connected.getClient());
                } else if(pkt instanceof ClientDisconnectedPacket) {
                    ClientDisconnectedPacket connected = (ClientDisconnectedPacket) pkt;

                    VoiceClient.serverConfig.clients.remove(connected.getClient());
                    VoiceClient.serverConfig.mutedClients.remove(connected.getClient());

                    final ClientPlayerEntity player = MinecraftClient.getInstance().player;
                    if(player != null) {
                        if(connected.getClient().equals(player.getUuid())) {
                            VoiceClient.disconnect();
                        }
                    }
                }
            } catch (IOException | IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            } finally {
                if (packetByteBuf != null) {
                    packetByteBuf.release();
                }
            }
        }
    }
}
