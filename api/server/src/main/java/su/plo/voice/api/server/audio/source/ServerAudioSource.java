package su.plo.voice.api.server.audio.source;

import org.jetbrains.annotations.NotNull;
import su.plo.lib.server.world.ServerPos3d;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.audio.source.AudioSource;
import su.plo.voice.api.server.audio.line.ServerSourceLine;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.packets.Packet;
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket;

import java.util.UUID;
import java.util.function.Predicate;

public interface ServerAudioSource extends AudioSource {

    @NotNull AddonContainer getAddon();

    @NotNull UUID getId();

    @NotNull ServerSourceLine getLine();

    void setLine(@NotNull ServerSourceLine line);

    int getState();

    @NotNull ServerPos3d getPosition();

    void setAngle(int angle);

    void setIconVisible(boolean visible);

    void setStereo(boolean stereo);

    boolean isIconVisible();

    void addFilter(Predicate<VoicePlayer> filter);

    void removeFilter(Predicate<VoicePlayer> filter);

    void sendAudioPacket(SourceAudioPacket packet, short distance);

    void sendPacket(Packet<?> packet, short distance);
}
