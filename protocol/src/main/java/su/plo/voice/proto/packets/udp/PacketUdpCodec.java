package su.plo.voice.proto.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketRegistry;
import su.plo.voice.proto.packets.PacketUtil;
import su.plo.voice.proto.packets.udp.bothbound.CustomPacket;
import su.plo.voice.proto.packets.udp.bothbound.PingPacket;
import su.plo.voice.proto.packets.udp.clientbound.SelfAudioInfoPacket;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class PacketUdpCodec {

    // magic number is used to filter packets received not from PV
    private static final int MAGIC_NUMBER = 0x4e9004e9;
    private static final PacketRegistry PACKETS = new PacketRegistry();

    static {
        int lastPacketId = 0x0;

        PACKETS.register(++lastPacketId, PingPacket.class);
        PACKETS.register(++lastPacketId, PlayerAudioPacket.class);
        PACKETS.register(++lastPacketId, SourceAudioPacket.class);
        PACKETS.register(++lastPacketId, SelfAudioInfoPacket.class);
        PACKETS.register(0x100, CustomPacket.class);
    }

    public static byte[] replaceSecret(byte[] data, UUID secret) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketUtil.writeUUID(out, secret);

        System.arraycopy(out.toByteArray(), 0, data, 5, 16);
        return data;
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
        if (in.readInt() != MAGIC_NUMBER) return Optional.empty(); // bad packet

        Packet<?> packet = PACKETS.byType(in.readByte());
        if (packet != null) {
            UUID secret = PacketUtil.readUUID(in);
            long timestamp = in.readLong();

            return Optional.of(new PacketUdp(secret, timestamp, packet, in));
        }

        return Optional.empty();
    }

    private PacketUdpCodec() {
    }
}
