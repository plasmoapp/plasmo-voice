package su.plo.voice.proto.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketRegistry;

import java.io.IOException;

public class PacketTcpCodec {

    private static final PacketRegistry PACKETS = new PacketRegistry();

    public static byte[] encode(Packet<?> packet) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(PACKETS.getType(packet));
        try {
            packet.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    public static Packet<?> decode(ByteArrayDataInput buf) throws IOException {
        Packet<?> packet = PACKETS.byType(buf.readByte());
        if (packet != null) {
            packet.read(buf);
            return packet;
        }

        return null;
    }

    private PacketTcpCodec() {
    }
}
