package su.plo.voice.proto.packets.tcp.clientbound;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public final class LanguagePacket implements Packet<ClientPacketTcpHandler> {

    private final static int MAX_SIZE = 32767;

    @Getter
    private String languageName;
    @Getter
    private Map<String, String> language;

    @Override
    public void read(ByteArrayDataInput in) throws IOException {
        this.languageName = in.readUTF();
        this.language = Maps.newHashMap();
        int size = PacketUtil.readSafeInt(in, 0, MAX_SIZE);
        for (int i = 0; i < size; i++) {
            String key = in.readUTF();
            String value = in.readUTF();
            language.put(key, value);
        }
    }

    @Override
    public void write(ByteArrayDataOutput out) throws IOException {
        out.writeUTF(checkNotNull(languageName));
        if (language.size() > MAX_SIZE) {
            throw new IllegalArgumentException("Language size is too big");
        }

        out.writeInt(language.size());
        for (Map.Entry<String, String> entry : language.entrySet()) {
            out.writeUTF(entry.getKey());
            out.writeUTF(entry.getValue());
        }
    }

    @Override
    public void handle(ClientPacketTcpHandler handler) {
        handler.handle(this);
    }
}
