package su.plo.voice.proto.data.player;

import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.*;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MinecraftGameProfile implements PacketSerializable {

    private UUID id;
    private String name;
    private List<Property> properties;

    @Override
    public void deserialize(ByteArrayDataInput in) throws IOException {
        this.id = PacketUtil.readUUID(in);
        this.name = in.readUTF();

        int length = PacketUtil.readSafeInt(in, 0, 100);
        this.properties = Lists.newArrayList();
        for (int i = 0; i < length; i++) {
            this.properties.add(new Property(in.readUTF(), in.readUTF(), in.readUTF()));
        }
    }

    @Override
    public void serialize(ByteArrayDataOutput out) throws IOException {
        PacketUtil.writeUUID(out, checkNotNull(id));
        out.writeUTF(checkNotNull(name));

        out.writeInt(properties.size());
        for (Property property : properties) {
            out.writeUTF(property.getName());
            out.writeUTF(property.getValue());
            out.writeUTF(property.getSignature());
        }
    }

    @Override
    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            MinecraftGameProfile gameProfile = (MinecraftGameProfile) object;
            return this.id.equals(gameProfile.id) && Objects.equals(this.name, gameProfile.getName());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(this.id, this.name);
    }

    @Data
    @RequiredArgsConstructor
    @ToString
    public static final class Property {

        private final String name;
        private final String value;
        private final String signature;
    }
}
