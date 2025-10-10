package su.plo.voice.proto.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;
import su.plo.voice.proto.packets.PacketRegistry;
import su.plo.voice.proto.packets.tcp.clientbound.ActivationRegisterPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ActivationUnregisterPacket;
import su.plo.voice.proto.packets.tcp.clientbound.AnimatedActionBarPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPlayerInfoPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket;
import su.plo.voice.proto.packets.tcp.clientbound.DistanceVisualizePacket;
import su.plo.voice.proto.packets.tcp.clientbound.LanguagePacket;
import su.plo.voice.proto.packets.tcp.clientbound.PlayerDisconnectPacket;
import su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoRequestPacket;
import su.plo.voice.proto.packets.tcp.clientbound.PlayerInfoUpdatePacket;
import su.plo.voice.proto.packets.tcp.clientbound.PlayerListPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SelfSourceInfoPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceInfoPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerAddPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayerRemovePacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLinePlayersListPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLineRegisterPacket;
import su.plo.voice.proto.packets.tcp.clientbound.SourceLineUnregisterPacket;
import su.plo.voice.proto.packets.tcp.serverbound.LanguageRequestPacket;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerActivationDistancesPacket;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerInfoPacket;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerStatePacket;
import su.plo.voice.proto.packets.tcp.serverbound.SourceInfoRequestPacket;

import java.io.IOException;
import java.util.Optional;

@SuppressWarnings({"unchecked"})
public class PacketTcpCodec {

    private static final PacketRegistry PACKETS = new PacketRegistry();

    static {
        int lastPacketId = 0x0;

        PACKETS.register(++lastPacketId, ConnectionPacket.class, ConnectionPacket::new);
        PACKETS.register(++lastPacketId, PlayerInfoRequestPacket.class, PlayerInfoRequestPacket::new);
        PACKETS.register(++lastPacketId, ConfigPacket.class, ConfigPacket::new);
        PACKETS.register(++lastPacketId, ConfigPlayerInfoPacket.class, ConfigPlayerInfoPacket::new);
        PACKETS.register(++lastPacketId, LanguageRequestPacket.class, LanguageRequestPacket::new);
        PACKETS.register(++lastPacketId, LanguagePacket.class, LanguagePacket::new);

        PACKETS.register(++lastPacketId, PlayerListPacket.class, PlayerListPacket::new);
        PACKETS.register(++lastPacketId, PlayerInfoUpdatePacket.class, PlayerInfoUpdatePacket::new);
        PACKETS.register(++lastPacketId, PlayerDisconnectPacket.class, PlayerDisconnectPacket::new);
        PACKETS.register(++lastPacketId, PlayerInfoPacket.class, PlayerInfoPacket::new);
        PACKETS.register(++lastPacketId, PlayerStatePacket.class, PlayerStatePacket::new);
        PACKETS.register(++lastPacketId, PlayerAudioEndPacket.class, PlayerAudioEndPacket::new);
        PACKETS.register(++lastPacketId, PlayerActivationDistancesPacket.class, PlayerActivationDistancesPacket::new);

        PACKETS.register(++lastPacketId, DistanceVisualizePacket.class, DistanceVisualizePacket::new);

        PACKETS.register(++lastPacketId, SourceInfoRequestPacket.class, SourceInfoRequestPacket::new);
        PACKETS.register(++lastPacketId, SourceInfoPacket.class, SourceInfoPacket::new);
        PACKETS.register(++lastPacketId, SelfSourceInfoPacket.class, SelfSourceInfoPacket::new);
        PACKETS.register(++lastPacketId, SourceAudioEndPacket.class, SourceAudioEndPacket::new);

        PACKETS.register(++lastPacketId, ActivationRegisterPacket.class, ActivationRegisterPacket::new);
        PACKETS.register(++lastPacketId, ActivationUnregisterPacket.class, ActivationUnregisterPacket::new);

        PACKETS.register(++lastPacketId, SourceLineRegisterPacket.class, SourceLineRegisterPacket::new);
        PACKETS.register(++lastPacketId, SourceLineUnregisterPacket.class, SourceLineUnregisterPacket::new);
        PACKETS.register(++lastPacketId, SourceLinePlayerAddPacket.class, SourceLinePlayerAddPacket::new);
        PACKETS.register(++lastPacketId, SourceLinePlayerRemovePacket.class, SourceLinePlayerRemovePacket::new);
        PACKETS.register(++lastPacketId, SourceLinePlayersListPacket.class, SourceLinePlayersListPacket::new);

        PACKETS.register(++lastPacketId, AnimatedActionBarPacket.class, AnimatedActionBarPacket::new);
    }

    public static byte[] encode(Packet<?> packet) {
        int type = PACKETS.getType(packet);
        if (type < 0) return null;

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(type);
        try {
            packet.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static <T extends PacketHandler> Optional<Packet<T>> decode(ByteArrayDataInput buf) throws IOException {
        Packet<T> packet = (Packet<T>) PACKETS.byType(buf.readByte());
        if (packet != null) {
            packet.read(buf);
            return Optional.of(packet);
        }

        return Optional.empty();
    }

    private PacketTcpCodec() {
    }
}
