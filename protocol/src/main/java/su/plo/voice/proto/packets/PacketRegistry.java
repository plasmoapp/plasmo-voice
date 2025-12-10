package su.plo.voice.proto.packets;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.function.Supplier;

public class PacketRegistry {

    private final Int2ObjectOpenHashMap<Supplier<? extends Packet<?>>> packetFactoryById = new Int2ObjectOpenHashMap<>();
    private final Object2IntOpenHashMap<Class<? extends Packet<?>>> packetIdByType = new Object2IntOpenHashMap<>();

    public void register(
            int packetId,
            Class<? extends Packet<?>> clazz,
            Supplier<? extends Packet<?>> factory
    ) {
        packetFactoryById.put(packetId, factory);
        packetIdByType.put(clazz, packetId);
    }

    public Packet<?> byType(int type) {
        Supplier<? extends Packet<?>> packetFactory = packetFactoryById.get(type);
        if (packetFactory != null) {
            try {
                return packetFactory.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public int getType(Packet<?> packet) {
        return packetIdByType.getOrDefault(packet.getClass(), -1);
    }
}
