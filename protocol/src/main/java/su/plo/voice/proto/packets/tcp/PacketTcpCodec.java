package su.plo.voice.proto.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;
import su.plo.voice.proto.packets.PacketRegistry;
import su.plo.voice.proto.packets.tcp.clientbound.*;
import su.plo.voice.proto.packets.tcp.serverbound.*;

import java.io.IOException;
import java.util.Optional;

@SuppressWarnings({"UnstableApiUsage", "unchecked"})
public class PacketTcpCodec {

    private static final PacketRegistry PACKETS = new PacketRegistry();

    static {
        int lastPacketId = 0x0;

        PACKETS.register(++lastPacketId, ConnectionPacket.class);
        PACKETS.register(++lastPacketId, PlayerInfoRequestPacket.class);
        PACKETS.register(++lastPacketId, ConfigPacket.class);
        PACKETS.register(++lastPacketId, ConfigPlayerInfoPacket.class);
        PACKETS.register(++lastPacketId, LanguageRequestPacket.class);
        PACKETS.register(++lastPacketId, LanguagePacket.class);

        PACKETS.register(++lastPacketId, PlayerListPacket.class);
        PACKETS.register(++lastPacketId, PlayerInfoUpdatePacket.class);
        PACKETS.register(++lastPacketId, PlayerDisconnectPacket.class);
        PACKETS.register(++lastPacketId, PlayerInfoPacket.class);
        PACKETS.register(++lastPacketId, PlayerStatePacket.class);
        PACKETS.register(++lastPacketId, PlayerAudioEndPacket.class);
        PACKETS.register(++lastPacketId, PlayerActivationDistancesPacket.class);

        PACKETS.register(++lastPacketId, DistanceVisualizePacket.class);

        PACKETS.register(++lastPacketId, SourceInfoRequestPacket.class);
        PACKETS.register(++lastPacketId, SourceInfoPacket.class);
        PACKETS.register(++lastPacketId, SelfSourceInfoPacket.class);
        PACKETS.register(++lastPacketId, SourceAudioEndPacket.class);

        PACKETS.register(++lastPacketId, ActivationRegisterPacket.class);
        PACKETS.register(++lastPacketId, ActivationUnregisterPacket.class);

        PACKETS.register(++lastPacketId, SourceLineRegisterPacket.class);
        PACKETS.register(++lastPacketId, SourceLineUnregisterPacket.class);
        PACKETS.register(++lastPacketId, SourceLinePlayerAddPacket.class);
        PACKETS.register(++lastPacketId, SourceLinePlayerRemovePacket.class);
        PACKETS.register(++lastPacketId, SourceLinePlayersListPacket.class);

        PACKETS.register(++lastPacketId, AnimatedActionBarPacket.class);
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
