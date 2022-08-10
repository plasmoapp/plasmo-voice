package su.plo.voice.proto.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketRegistry;
import su.plo.voice.proto.packets.PacketUtil;
import su.plo.voice.proto.packets.udp.bothbound.CustomPacket;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class PacketUdpCodec {

    // magic number is used to filter packets received not from PV
    private static final int MAGIC_NUMBER = 0x4e9004e9;
    private static final PacketRegistry PACKETS = new PacketRegistry();

    static {
        PACKETS.register(0x1, PingPacket.class);
        PACKETS.register(0x2, PlayerAudioPacket.class);
        PACKETS.register(0x100, CustomPacket.class);
    }

    public static byte[] encode(Packet<?> packet, UUID secret) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        int type = PACKETS.getType(packet);
        if (type < 0) return null;

        out.writeInt(MAGIC_NUMBER);
        out.writeByte(type);
        PacketUtil.writeUUID(out, secret);
        out.writeLong(System.currentTimeMillis());

        try {
            packet.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    public static Optional<PacketUdp> decode(ByteArrayDataInput in) throws IOException {
        if (in.readInt() != MAGIC_NUMBER) return null; // bad packet

        Packet<?> packet = PACKETS.byType(in.readByte());
        if (packet != null) {
            UUID secret = PacketUtil.readUUID(in);
            long timestamp = in.readLong();
            packet.read(in);

            return Optional.of(new PacketUdp(secret, timestamp, packet));
        }

        return Optional.empty();
    }

    private PacketUdpCodec() {
    }
}
