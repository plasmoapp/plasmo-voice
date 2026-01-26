package su.plo.voice.proto.data.audio.codec;

import com.google.common.collect.Maps;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import su.plo.voice.proto.packets.PacketSerializable;
import su.plo.voice.proto.packets.PacketUtil;

import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CodecInfo implements PacketSerializable {

    @Getter
    protected String name;
    @Getter
    protected Map<String, String> params;

    @Override
    public void deserialize(ByteArrayDataInput in) throws IOException {
        this.name = in.readUTF();

        this.params = Maps.newHashMap();
        int size = PacketUtil.readSafeInt(in, 0, 128);
        for (int i = 0; i < size; i++) {
            params.put(in.readUTF(), in.readUTF());
        }
    }

    @Override
    public void serialize(ByteArrayDataOutput out) {
        out.writeUTF(checkNotNull(name));
        checkNotNull(params);

        out.writeInt(params.size());
        params.forEach((key, value) -> {
            out.writeUTF(key);
            out.writeUTF(value);
        });
    }
}
