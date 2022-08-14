package su.plo.voice.api.audio.source;


import org.jetbrains.annotations.NotNull;
import su.plo.voice.proto.packets.udp.bothbound.BaseAudioPacket;

import java.util.UUID;

// TODO: doc
public interface AudioSource {

    @NotNull UUID getId();

    @NotNull String getCodec();

    @NotNull Type getType();

    void process(BaseAudioPacket packet, short distance);

    enum Type {
        PLAYER,
        ENTITY,
        STATIC,
        DIRECT,
        CUSTOM
    }
}
