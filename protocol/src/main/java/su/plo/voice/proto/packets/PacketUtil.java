package su.plo.voice.proto.packets;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketUtil {

    public static void writeUUID(ByteArrayDataOutput out, UUID uuid) {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    public static UUID readUUID(ByteArrayDataInput in) {
        return new UUID(in.readLong(), in.readLong());
    }

    public static void writeUUIDList(ByteArrayDataOutput out, List<UUID> uuids) {
        out.writeInt(uuids.size());
        for (UUID uuid : uuids) {
            writeUUID(out, uuid);
        }
    }

    public static List<UUID> readUUIDList(ByteArrayDataInput in) {
        int size = in.readInt();
        List<UUID> uuids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) uuids.add(readUUID(in));
        return uuids;
    }

    public static void writeIntList(ByteArrayDataOutput out, List<Integer> list) {
        out.writeInt(list.size());
        list.forEach(out::writeInt);
    }

    public static List<Integer> readIntList(ByteArrayDataInput in) {
        int size = in.readInt();
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(in.readInt());
        return list;
    }

    public static void writeNullableString(ByteArrayDataOutput out, @Nullable String str) {
        out.writeBoolean(str != null);
        if (str != null) out.writeUTF(str);
    }

    public static @Nullable String readNullableString(ByteArrayDataInput in) {
        if (in.readBoolean()) return in.readUTF();
        return null;
    }

    private PacketUtil() {
    }
}
