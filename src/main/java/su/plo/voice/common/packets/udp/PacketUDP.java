package su.plo.voice.common.packets.udp;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import su.plo.voice.common.packets.Packet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class PacketUDP {
    private final long timestamp;
    private final long ttl;
    private Packet packet;
    private InetAddress address;
    private int port;

    private Class<? extends Packet> packetClass;
    private ByteArrayDataInput input;

    public PacketUDP(Packet packet, String secret) {
        this(packet);
    }

    public PacketUDP(Packet packet) {
        this();
        this.packet = packet;
        this.packetClass = packet.getClass();
    }

    private PacketUDP() {
        this.timestamp = System.currentTimeMillis();
        this.ttl = 2000L;
        this.port = -1;
    }

    public Packet getPacket() throws Exception {
        if (packet == null) decodePacket();
        return packet;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getTTL() {
        return ttl;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public InetSocketAddress getInetSocketAddress() {
        return new InetSocketAddress(address, port);
    }

    public Class<? extends Packet> getPacketClass() {
        return packetClass;
    }

    private synchronized void decodePacket() throws Exception {
        if (packetClass == null || input == null) return;

        this.packet = packetClass.getDeclaredConstructor().newInstance();
        packet.read(input);
        this.input = null;
    }

    private static final Map<Byte, Class<? extends Packet>> packetRegistry;

    static {
        packetRegistry = new HashMap<>();
        packetRegistry.put((byte) 0, VoiceClientPacket.class);
        packetRegistry.put((byte) 1, VoiceServerPacket.class);
        packetRegistry.put((byte) 2, AuthPacket.class);
        packetRegistry.put((byte) 3, AuthPacketAck.class);
        packetRegistry.put((byte) 4, VoiceEndClientPacket.class);
        packetRegistry.put((byte) 5, VoiceEndServerPacket.class);
        packetRegistry.put((byte) 6, PingPacket.class);
    }

    private static byte getPacketType(Packet packet) {
        for (Map.Entry<Byte, Class<? extends Packet>> entry : packetRegistry.entrySet()) {
            if (packet.getClass().equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    public static PacketUDP read(DatagramSocket socket) throws Exception {
        DatagramPacket packet = new DatagramPacket(new byte[4096], 4096);
        socket.receive(packet);

        byte[] data = new byte[packet.getLength()];
        System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

        ByteArrayDataInput input = ByteStreams.newDataInput(data);
        byte packetType = input.readByte();
        Class<? extends Packet> packetClass = packetRegistry.get(packetType);
        if (packetClass == null) {
            throw new InstantiationException("Could not find packet with ID " + packetType);
        }

        PacketUDP message = new PacketUDP();
        message.address = packet.getAddress();
        message.port = packet.getPort();
        message.packetClass = packetClass;
        message.input = input;

        return message;
    }

    public static byte[] write(Packet packet) throws IOException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        byte type = getPacketType(packet);

        if (type < 0) {
            throw new IllegalArgumentException("Packet type not found");
        }

        out.writeByte(type);
        packet.write(out);

        return out.toByteArray();
    }
}
