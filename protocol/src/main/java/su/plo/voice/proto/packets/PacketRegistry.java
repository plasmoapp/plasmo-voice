package su.plo.voice.proto.packets;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class PacketRegistry {

    private final Int2ObjectOpenHashMap<Supplier<? extends Packet<?>>> packetFactoryById = new Int2ObjectOpenHashMap<>();
    private final Object2IntOpenHashMap<Class<? extends Packet<?>>> packetIdByType = new Object2IntOpenHashMap<>();
    private final Int2ObjectOpenHashMap<PacketDirection> packetDirectionById = new Int2ObjectOpenHashMap<>();

    public void register(
            int packetId,
            PacketDirection direction,
            Class<? extends Packet<?>> clazz,
            Supplier<? extends Packet<?>> factory
    ) {
        packetFactoryById.put(packetId, factory);
        packetIdByType.put(clazz, packetId);
        packetDirectionById.put(packetId, direction);
    }

    public Packet<?> byType(int type) {
        return byType(type, PacketDirection.ANY);
    }

    public Packet<?> byType(int type, @NotNull PacketDirection direction) {
        PacketDirection packetDirection = packetDirectionById.get(type);
        if (packetDirection == null || !packetDirection.accepts(direction)) {
            return null;
        }

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
