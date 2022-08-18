package su.plo.voice.server.connection;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.S2CPlayChannelEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.apache.logging.log4j.LogManager;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.server.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class FabricServerChannelHandler extends BaseServerChannelHandler implements ServerPlayNetworking.PlayChannelHandler, S2CPlayChannelEvents.Register {

    private final Map<UUID, PlayerChannelHandler> channels = Maps.newHashMap();

    public FabricServerChannelHandler(PlasmoVoiceServer voiceServer) {
        super(voiceServer);
    }

    @Override
    public void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        byte[] data = new byte[buf.readableBytes()];
        buf.duplicate().readBytes(data);

        try {
            PacketTcpCodec.decode(ByteStreams.newDataInput(data))
                    .ifPresent(packet -> {
                        LogManager.getLogger().info("Channel packet received {}", packet);

                        VoicePlayer voicePlayer = voiceServer.getPlayerManager().wrap(player);

                        PlayerChannelHandler channel = channels.computeIfAbsent(
                                player.getUUID(),
                                (playerId) -> new PlayerChannelHandler(voiceServer, voicePlayer)
                        );

                        packet.handle(channel);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onChannelRegister(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server, List<ResourceLocation> channels) {
        voiceServer.getPlayerManager().getPlayerById(handler.getPlayer().getUUID()).ifPresent(player ->
                handleRegisterChannels(
                        channels.stream().map(ResourceLocation::toString).collect(Collectors.toList()),
                        player
                )
        );
    }

    @EventSubscribe
    public void onPlayerQuit(PlayerQuitEvent event) {
        channels.remove(event.getPlayerId());
    }
}
