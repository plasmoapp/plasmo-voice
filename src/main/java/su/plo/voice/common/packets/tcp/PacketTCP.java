package su.plo.voice.common.packets.tcp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class PacketTCP {
    private static final Map<Integer, Class<? extends Packet>> packetRegistry;

    static {
        packetRegistry = new HashMap<>();
        packetRegistry.put(0, ClientsListPacket.class);
        packetRegistry.put(1, ClientMutedPacket.class);
        packetRegistry.put(2, ClientUnmutedPacket.class);
        packetRegistry.put(3, ClientConnectedPacket.class);
        packetRegistry.put(4, ClientDisconnectedPacket.class);
        packetRegistry.put(5, ConfigPacket.class);
        packetRegistry.put(6, ServerConnectPacket.class);
        packetRegistry.put(7, ClientConnectPacket.class);
    }

    private static int getPacketType(Packet packet) {
        for (Map.Entry<Integer, Class<? extends Packet>> entry : packetRegistry.entrySet()) {
            if (packet.getClass().equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public static byte[] write(Packet packet) throws IOException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(getPacketType(packet));
        packet.write(out);
        return out.toByteArray();
    }

    public static Packet read(ByteArrayDataInput buf) throws IllegalAccessException, InstantiationException, IOException, NoSuchMethodException, InvocationTargetException {
        int packetType = buf.readByte();
        Class<? extends Packet> packetClass = packetRegistry.get(packetType);
        if (packetClass == null) {
            throw new InstantiationException("Could not find packet with ID " + packetType);
        }
        Packet packet = packetClass.getDeclaredConstructor().newInstance();
        packet.read(buf);

        return packet;
    }
}
