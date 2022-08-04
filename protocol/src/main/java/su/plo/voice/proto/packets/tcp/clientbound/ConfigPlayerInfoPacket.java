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
    private Map<String, Integer> playerInfo = new HashMap<>();

    private ConfigPlayerInfoPacket(@NotNull Map<String, Integer> playerInfo) {
        this.playerInfo = checkNotNull(playerInfo, "playerInfo cannot be null");
    }

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        int size = in.readInt();
        for (int i = 0; i < size; size++) {
            String key = in.readUTF();
            int value = in.readInt();

            playerInfo.put(key, value);
        }
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeInt(playerInfo.size());
        for (Map.Entry<String, Integer> entry : playerInfo.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeInt(entry.getValue());
        }
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
