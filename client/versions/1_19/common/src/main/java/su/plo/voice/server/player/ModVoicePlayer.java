package su.plo.voice.server.player;

import io.netty.buffer.Unpooled;
import lombok.ToString;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.event.connection.TcpPacketSendEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.tcp.PacketTcpCodec;
import su.plo.voice.proto.packets.tcp.clientbound.ClientPacketTcpHandler;
import su.plo.voice.server.ModVoiceServer;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@ToString(of = "player")
public final class ModVoicePlayer implements VoicePlayer {

    private final PlasmoVoiceServer voiceServer;
    private final ServerPlayer player;

    public ModVoicePlayer(@NotNull PlasmoVoiceServer voiceServer, @NotNull ServerPlayer player) {
        this.voiceServer = checkNotNull(voiceServer, "voiceServer");
        this.player = checkNotNull(player, "player");
    }

    @Override
    public @NotNull UUID getUUID() {
        return player.getUUID();
    }

    @Override
    public <T> T getServerPlayer() {
        return (T) player;
    }

    @Override
    public void sendPacket(Packet<ClientPacketTcpHandler> packet) {
        byte[] encoded = PacketTcpCodec.encode(packet);

        TcpPacketSendEvent event = new TcpPacketSendEvent(this, packet);
        voiceServer.getEventBus().call(event);
        if (event.isCancelled()) return;

        player.connection.send(new ClientboundCustomPayloadPacket(
                ModVoiceServer.CHANNEL,
                new FriendlyByteBuf(Unpooled.wrappedBuffer(encoded))
        ));

        LogManager.getLogger().info("packet {} sent to {}", packet, this);
    }
}
