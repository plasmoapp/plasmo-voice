package su.plo.voice.proto.packets;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.serializer.PacketSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketUtil {

    public static int readSafeInt(ByteArrayDataInput in, int minInt, int maxInt) throws IOException {
        int value = in.readInt();
        if (value < minInt || value > maxInt) {
            throw new IOException("Invalid int value (min: " + minInt + ", max: " + maxInt + ", value: " + value + ")");
        }
        return value;
    }

    public static void writeBytes(ByteArrayDataOutput out, byte[] bytes) {
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public static byte[] readBytes(ByteArrayDataInput in, int max) throws IOException {
        byte[] bytes = new byte[readSafeInt(in, 0, max)];
        in.readFully(bytes);
        return bytes;
    }

    public static void writeUUID(ByteArrayDataOutput out, UUID uuid) {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }

    public static UUID readUUID(ByteArrayDataInput in) {
        return new UUID(in.readLong(), in.readLong());
    }

    public static byte[] getUUIDBytes(UUID uuid) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        writeUUID(out, uuid);
        return out.toByteArray();
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

    public static <T> void writeNullable(
            @NotNull ByteArrayDataOutput buffer,
            @NotNull PacketSerializer<T> serializer,
            @Nullable T obj
    ) throws IOException {
        buffer.writeBoolean(obj != null);
        if (obj != null) serializer.serialize(obj, buffer);
    }

    public static <T> @Nullable T readNullable(
            @NotNull ByteArrayDataInput buffer,
            @NotNull PacketSerializer<T> serializer
    ) throws IOException {
        if (buffer.readBoolean()) return serializer.deserialize(buffer);
        return null;
    }

    public static <T> void writeList(
            @NotNull ByteArrayDataOutput buffer,
            @NotNull PacketSerializer<T> serializer,
            @NotNull List<T> list
    ) throws IOException {
        buffer.writeInt(list.size());
        for (T obj : list) {
            serializer.serialize(obj, buffer);
        }
    }

    public static <T> List<T> readList(@NotNull ByteArrayDataInput buffer, @NotNull PacketSerializer<T> serializer) throws IOException {
        int size = buffer.readInt();
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(serializer.deserialize(buffer));
        }

        return list;
    }

    private PacketUtil() {
    }
}
