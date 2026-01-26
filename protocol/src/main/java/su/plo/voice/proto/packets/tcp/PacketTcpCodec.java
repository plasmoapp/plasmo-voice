package su.plo.voice.proto.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketDirection;
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

        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, ConnectionPacket.class, ConnectionPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, PlayerInfoRequestPacket.class, PlayerInfoRequestPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, ConfigPacket.class, ConfigPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, ConfigPlayerInfoPacket.class, ConfigPlayerInfoPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.SERVER, LanguageRequestPacket.class, LanguageRequestPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, LanguagePacket.class, LanguagePacket::new);

        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, PlayerListPacket.class, PlayerListPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, PlayerInfoUpdatePacket.class, PlayerInfoUpdatePacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, PlayerDisconnectPacket.class, PlayerDisconnectPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.SERVER, PlayerInfoPacket.class, PlayerInfoPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.SERVER, PlayerStatePacket.class, PlayerStatePacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.SERVER, PlayerAudioEndPacket.class, PlayerAudioEndPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.SERVER, PlayerActivationDistancesPacket.class, PlayerActivationDistancesPacket::new);

        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, DistanceVisualizePacket.class, DistanceVisualizePacket::new);

        PACKETS.register(++lastPacketId, PacketDirection.SERVER, SourceInfoRequestPacket.class, SourceInfoRequestPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, SourceInfoPacket.class, SourceInfoPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, SelfSourceInfoPacket.class, SelfSourceInfoPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, SourceAudioEndPacket.class, SourceAudioEndPacket::new);

        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, ActivationRegisterPacket.class, ActivationRegisterPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, ActivationUnregisterPacket.class, ActivationUnregisterPacket::new);

        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, SourceLineRegisterPacket.class, SourceLineRegisterPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, SourceLineUnregisterPacket.class, SourceLineUnregisterPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, SourceLinePlayerAddPacket.class, SourceLinePlayerAddPacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, SourceLinePlayerRemovePacket.class, SourceLinePlayerRemovePacket::new);
        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, SourceLinePlayersListPacket.class, SourceLinePlayersListPacket::new);

        PACKETS.register(++lastPacketId, PacketDirection.CLIENT, AnimatedActionBarPacket.class, AnimatedActionBarPacket::new);
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
        return decode(buf, PacketDirection.ANY);
    }

    public static <T extends PacketHandler> Optional<Packet<T>> decode(
            @NotNull ByteArrayDataInput buf,
            @NotNull PacketDirection direction
    ) throws IOException {
        Packet<T> packet = (Packet<T>) PACKETS.byType(buf.readByte(), direction);
        if (packet != null) {
            packet.read(buf);
            return Optional.of(packet);
        }

        return Optional.empty();
    }

    private PacketTcpCodec() {
    }
}
