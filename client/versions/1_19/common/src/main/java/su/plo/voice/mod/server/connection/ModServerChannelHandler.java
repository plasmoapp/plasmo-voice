package su.plo.voice.mod.server.connection;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.server.connection.BaseServerChannelHandler;
import su.plo.voice.server.connection.PlayerChannelHandler;
import su.plo.voice.server.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public abstract class ModServerChannelHandler extends BaseServerChannelHandler {

    private final Map<UUID, PlayerChannelHandler> channels = Maps.newHashMap();

    protected ModServerChannelHandler(PlasmoVoiceServer voiceServer) {
        super(voiceServer);
    }

    public void clear() {
        channels.clear();
    }

    protected void receive(ServerPlayer player, FriendlyByteBuf buf) {
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

                        channel.handlePacket(packet);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventSubscribe
    public void onPlayerQuit(PlayerQuitEvent event) {
        channels.remove(event.getPlayerId());
    }
}
