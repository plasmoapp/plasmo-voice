package su.plo.voice.server.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import su.plo.voice.common.packets.Packet;
import su.plo.voice.common.packets.tcp.ClientConnectPacket;
import su.plo.voice.common.packets.tcp.PacketTCP;
import su.plo.voice.server.VoiceServer;
import su.plo.voice.server.config.ServerConfigFabric;
import su.plo.voice.server.socket.SocketServerUDP;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ServerNetworkHandlerFabric extends ServerNetworkHandler {
    private static final ResourceLocation FML_HANDSHAKE = new ResourceLocation("fml:handshake");
    private final HashMap<UUID, ScheduledFuture> kickTimeouts = new HashMap<>();
    private final Set<UUID> fabricPlayers = new HashSet<>();

    @Override
    public boolean isVanillaPlayer(ServerPlayer player) {
        return !fabricPlayers.contains(player.getUUID());
    }

    @Override
    public void handleJoin(ServerPlayer player) {
        super.handleJoin(player);

        ServerConfigFabric config = (ServerConfigFabric) VoiceServer.getServerConfig();

        if (config.isClientModRequired()) {
            kickTimeouts.put(player.getUUID(), scheduler.schedule(() -> {
                if (!SocketServerUDP.clients.containsKey(player.getUUID())) {
                    if (VoiceServer.isLogsEnabled()) {
                        VoiceServer.LOGGER.info("Player {} does not have the mod installed!", player.getGameProfile().getName());
                    }

                    VoiceServer.getServer().execute(() ->
                            player.connection.disconnect(new TextComponent(VoiceServer.getInstance().getMessage("mod_missing_kick_message")))
                    );
                }
            }, (config.getClientModCheckTimeout() / 20) * 1000L, TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void handleQuit(ServerPlayer player) {
        if (kickTimeouts.containsKey(player.getUUID())) {
            kickTimeouts.get(player.getUUID()).cancel(true);
            kickTimeouts.remove(player.getUUID());
        }

        fabricPlayers.remove(player.getUUID());

        super.handleQuit(player);
    }

    @Override
    public void handleRegisterChannels(List<ResourceLocation> channels, ServerPlayer player) {
        if (channels.size() > 0) {
            if (!playerToken.containsKey(player.getUUID()) && channels.contains(VoiceServer.PLASMO_VOICE)
                    && !SocketServerUDP.clients.containsKey(player.getUUID())) {
                if (kickTimeouts.containsKey(player.getUUID())) {
                    kickTimeouts.get(player.getUUID()).cancel(true);
                    kickTimeouts.remove(player.getUUID());
                }

                ServerNetworkHandler.reconnectClient(player);

                if (!channels.contains(FML_HANDSHAKE)) {
                    fabricPlayers.add(player.getUUID());
                }
            }
            // only works on >= 1.1.0 client
//            else {
//                if (((ServerConfigFabric) VoiceServer.getServerConfig()).isClientModRequired() &&
//                        !playerToken.containsKey(player.getUUID())
//                        && !channels.contains(VoiceServer.PLASMO_VOICE)) {
//                    if (VoiceServer.isLogsEnabled()) {
//                        VoiceServer.LOGGER.info("Player {} does not have the mod installed!", player.getGameProfile().getName());
//                    }
//                    player.connection.disconnect(new TextComponent(VoiceServer.getInstance().getMessage("mod_missing_kick_message")));
//                }
//            }
        }
    }

    public void handle(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        try {
            byte[] data = new byte[buf.readableBytes()];
            buf.duplicate().readBytes(data);
            ByteArrayDataInput in = ByteStreams.newDataInput(data);

            Packet pkt = PacketTCP.read(in);
            if (pkt instanceof ClientConnectPacket packet) {
                this.handle(packet, player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
