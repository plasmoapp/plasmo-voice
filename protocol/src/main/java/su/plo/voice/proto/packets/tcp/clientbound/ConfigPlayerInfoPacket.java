package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.Packet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@NoArgsConstructor
@ToString
public class ConfigPlayerInfoPacket implements Packet<ClientPacketTcpHandler> {

    @Getter
    private Map<String, Boolean> permissions = new HashMap<>();

    public ConfigPlayerInfoPacket(@NotNull Map<String, Boolean> permissions) {
        this.permissions = checkNotNull(permissions, "playerPermissions cannot be null");
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        int size = in.readInt();
        for (int i = 0; i < size; size++) {
            String key = in.readUTF();
            boolean value = in.readBoolean();

            permissions.put(key, value);
        }
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeInt(permissions.size());
        permissions.forEach((permission, value) -> {
            out.writeUTF(permission);
            out.writeBoolean(value);
        });
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
