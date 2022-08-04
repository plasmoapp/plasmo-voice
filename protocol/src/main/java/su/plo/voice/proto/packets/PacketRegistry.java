package su.plo.voice.proto.packets;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    private final Map<Integer, Class<? extends Packet<?>>> packets = new HashMap<>();
    private final Map<Class<? extends Packet<?>>, Integer> packetIdByType = new HashMap<>();

    public void register(int packetId, Class<? extends Packet<?>> clazz) {
        packets.put(packetId, clazz);
        packetIdByType.put(clazz, packetId);
    }

    public Packet<?> byType(int type) {
        Class<? extends Packet<?>> clazz = packets.get(type);
        if (clazz != null) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public int getType(Packet<?> packet) {
        Integer id = packetIdByType.get(packet.getClass());
        if (id == null) return -1;

        return id;
    }
}
