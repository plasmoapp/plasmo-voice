package su.plo.voice.proto.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
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

        PACKETS.register(++lastPacketId, PingPacket.class, PingPacket::new);
        PACKETS.register(++lastPacketId, PlayerAudioPacket.class, PlayerAudioPacket::new);
        PACKETS.register(++lastPacketId, SourceAudioPacket.class, SourceAudioPacket::new);
        PACKETS.register(++lastPacketId, SelfAudioInfoPacket.class, SelfAudioInfoPacket::new);
        PACKETS.register(0x100, CustomPacket.class, CustomPacket::new);
    }

    public static byte[] replaceSecret(byte[] data, UUID secret) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketUtil.writeUUID(out, secret);

        System.arraycopy(out.toByteArray(), 0, data, 5, 16);
        return data;
    }

    public static void encode(@NotNull Packet<?> packet, @NotNull UUID secret, @NotNull ByteArrayDataOutput out) throws IOException {
        int type = PACKETS.getType(packet);
        if (type < 0) throw new IOException("Unknown packet type");

        out.writeInt(MAGIC_NUMBER);
        out.writeByte(type);
        PacketUtil.writeUUID(out, secret);
        out.writeLong(System.currentTimeMillis());

        packet.write(out);
    }

    public static byte[] encode(@NotNull Packet<?> packet, @NotNull UUID secret) throws IOException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        encode(packet, secret, out);

        return out.toByteArray();
    }

    public static @NotNull PacketUdp decodeThrowing(@NotNull ByteArrayDataInput in) throws IOException {
        try {
            if (in.readInt() != MAGIC_NUMBER) throw new IOException("Magic number in packet header doesn't match"); // bad packet
        } catch (Exception e) {
            throw new IOException("Failed to read magic number", e);
        }

        Packet<?> packet = PACKETS.byType(in.readByte());
        if (packet == null) throw new IOException("Unknown packet type");

        UUID secret = PacketUtil.readUUID(in);
        long timestamp = in.readLong();

        return new PacketUdp(secret, timestamp, packet, in);
    }

    public static Optional<PacketUdp> decode(ByteArrayDataInput in) throws IOException {
        try {
            if (in.readInt() != MAGIC_NUMBER) return Optional.empty(); // bad packet
        } catch (Exception e) {
            return Optional.empty();
        }

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
