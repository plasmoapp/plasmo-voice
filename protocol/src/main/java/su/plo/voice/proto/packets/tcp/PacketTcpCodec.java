package su.plo.voice.proto.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketHandler;
import su.plo.voice.proto.packets.PacketRegistry;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ConfigPlayerInfoPacket;
import su.plo.voice.proto.packets.tcp.clientbound.ConnectionPacket;

import java.io.IOException;
import java.util.Optional;

public class PacketTcpCodec {

    private static final PacketRegistry PACKETS = new PacketRegistry();

    static {
        PACKETS.register(0x1, ConnectionPacket.class);
        PACKETS.register(0x2, ConfigPacket.class);
        PACKETS.register(0x3, ConfigPlayerInfoPacket.class);
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
