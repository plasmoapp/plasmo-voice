package su.plo.voice.mixin;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.mojang.brigadier.CommandDispatcher;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.server.SCommandListPacket;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su.plo.voice.Voice;
import su.plo.voice.VoiceServerConfig;
import su.plo.voice.commands.ForgeClientCommandSource;
import su.plo.voice.common.entities.MutedEntity;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.*;
import su.plo.voice.event.VoiceChatCommandEvent;
import su.plo.voice.socket.SocketClientUDP;
import su.plo.voice.socket.SocketClientUDPQueue;
import su.plo.voice.sound.ThreadSoundQueue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

@Mixin(ClientPlayNetHandler.class)
public abstract class MixinClientPlayNetHandler {
    @Shadow @Final private NetworkManager connection;

    @Shadow private Minecraft minecraft;

    @Shadow private CommandDispatcher<ISuggestionProvider> commands;

    @Shadow @Final private ClientSuggestionProvider suggestionsProvider;

    @Inject(method = "handleCommands", at = @At("RETURN"))
    private void onOnCommandTree(SCommandListPacket packet, CallbackInfo info) {
        // Add the commands to the vanilla dispatcher for completion.
        // It's done here because both the server and the client commands have
        // to be in the same dispatcher and completion results.
        VoiceChatCommandEvent.addCommands((CommandDispatcher) this.commands, (ForgeClientCommandSource) this.suggestionsProvider);
    }

    @Inject(at = @At("HEAD"), method = "handleCustomPayload", cancellable = true)
    public void onCustomPayload(SCustomPayloadPlayPacket packet, CallbackInfo info) {
        ResourceLocation identifier = packet.getIdentifier();
        if(identifier.equals(Voice.PLASMO_VOICE)) {
            info.cancel();

            PacketBuffer packetByteBuf = null;
            try {
                packetByteBuf = packet.getData();
                byte[] data = new byte[packetByteBuf.readableBytes()];
                packetByteBuf.duplicate().readBytes(data);
                ByteArrayDataInput buf = ByteStreams.newDataInput(data);

                Packet pkt = PacketTCP.read(buf);
                if(pkt instanceof ServerConnectPacket) {
                    Voice.disconnect();
                    Voice.socketUDP = null;

                    ServerConnectPacket connect = (ServerConnectPacket) pkt;
                    String ip = connect.getIp();

                    InetSocketAddress addr = (InetSocketAddress) this.connection.getRemoteAddress();
                    Inet4Address in4addr = (Inet4Address) addr.getAddress();
                    String[] ipSplit = in4addr.toString().split("/");

                    String serverIp = ipSplit[0];
                    if(ipSplit.length > 1) {
                        serverIp = ipSplit[1];
                    }

                    if(ip.equals("0.0.0.0")) {
                        ip = serverIp;
                    }

                    Voice.LOGGER.info("Connecting to " + minecraft.getCurrentServer().ip);

                    Voice.serverConfig = new VoiceServerConfig(connect.getToken(), ip,
                            connect.getPort(), connect.hasPriority());

                    this.connection.send(
                            new CCustomPayloadPacket(Voice.PLASMO_VOICE,
                                    new PacketBuffer(Unpooled.wrappedBuffer(PacketTCP.write(new ClientConnectPacket(connect.getToken(), Voice.version))))));
                } else if(pkt instanceof ConfigPacket) {
                    ConfigPacket config = (ConfigPacket) pkt;
                    Voice.serverConfig.update(config);

                    if(Voice.socketUDP == null) {
                        Voice.socketUDP = new SocketClientUDP();
                        Voice.socketUDP.start();
                    }
                } else if(pkt instanceof ClientMutedPacket) {
                    ClientMutedPacket muted = (ClientMutedPacket) pkt;
                    Voice.serverConfig.mutedClients.put(muted.getClient(), new MutedEntity(muted.getClient(), muted.getTo()));
                    ThreadSoundQueue queue = SocketClientUDPQueue.audioChannels.get(muted.getClient());
                    if(queue != null) {
                        queue.closeAndKill();
                        SocketClientUDPQueue.audioChannels.remove(muted.getClient());
                    }

                    Voice.LOGGER.info(muted.getClient().toString() + " muted");
                } else if(pkt instanceof ClientUnmutedPacket) {
                    ClientUnmutedPacket unmuted = (ClientUnmutedPacket) pkt;
                    Voice.serverConfig.mutedClients.remove(unmuted.getClient());

                    Voice.LOGGER.info(unmuted.getClient().toString() + " unmuted");
                } else if(pkt instanceof ClientsListPacket) {
                    ClientsListPacket list = (ClientsListPacket) pkt;

                    Voice.serverConfig.mutedClients.clear();
                    Voice.serverConfig.clients.clear();

                    List<String> mutedList = new ArrayList<>();

                    for(MutedEntity muted : list.getMuted()) {
                        mutedList.add(muted.uuid.toString());
                        Voice.serverConfig.mutedClients.put(muted.uuid, muted);
                    }
                    Voice.serverConfig.clients.addAll(list.getClients());

                    Voice.LOGGER.info("Clients: " + list.getClients().toString());
                    Voice.LOGGER.info("Muted clients: " + mutedList);
                } else if(pkt instanceof ClientConnectedPacket) {
                    ClientConnectedPacket connected = (ClientConnectedPacket) pkt;
                    if(connected.getMuted() != null) {
                        MutedEntity muted = connected.getMuted();
                        Voice.serverConfig.mutedClients.put(muted.uuid, muted);
                    }

                    Voice.serverConfig.clients.add(connected.getClient());
                } else if(pkt instanceof ClientDisconnectedPacket) {
                    ClientDisconnectedPacket connected = (ClientDisconnectedPacket) pkt;

                    Voice.serverConfig.clients.remove(connected.getClient());
                    Voice.serverConfig.mutedClients.remove(connected.getClient());
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
