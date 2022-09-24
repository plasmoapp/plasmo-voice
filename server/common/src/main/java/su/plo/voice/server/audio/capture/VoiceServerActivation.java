package su.plo.voice.server.audio.capture;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.event.EventSubscribe;
import su.plo.voice.api.server.PlasmoVoiceServer;
import su.plo.voice.api.server.audio.capture.ServerActivation;
import su.plo.voice.api.server.event.connection.TcpPacketReceivedEvent;
import su.plo.voice.api.server.event.connection.UdpPacketReceivedEvent;
import su.plo.voice.proto.data.audio.capture.VoiceActivation;
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket;
import su.plo.voice.proto.packets.udp.cllientbound.SourceAudioPacket;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class VoiceServerActivation extends VoiceActivation implements ServerActivation {

    private final PlasmoVoiceServer voiceServer;
    @Getter
    private final AddonContainer addon;

    @Nullable
    private Handler handler;

    public VoiceServerActivation(@NotNull PlasmoVoiceServer voiceServer,
                                 @NotNull AddonContainer addon,
                                 @NotNull String name,
                                 @NotNull String translation,
                                 @NotNull String icon,
                                 List<Integer> distances,
                                 int defaultDistance,
                                 boolean transitive,
                                 boolean stereoSupported,
                                 int weight) {
        super(name, translation, icon, distances, defaultDistance, stereoSupported, weight);

        this.voiceServer = voiceServer;
        this.addon = addon;
        this.transitive = transitive;
    }

    @Override
    public void setDistances(List<Integer> distances) {
        this.distances = checkNotNull(distances);
    }

    @Override
    public void setTransitive(boolean transitive) {
        this.transitive = transitive;
    }

    @Override
    public void setHandler(@Nullable Handler handler) {
        if (!addon.getInstance().isPresent()) return;

        if (handler != null && this.handler == null) {
            voiceServer.getEventBus().register(addon.getInstance().get(), this);
        }

        this.handler = handler;

        if (handler == null) {
            voiceServer.getEventBus().unregister(addon.getInstance().get(), this);
        }
    }

    @EventSubscribe
    public void onAudioPacket(@NotNull UdpPacketReceivedEvent event) {
        if (handler == null ||
                !(event.getPacket() instanceof SourceAudioPacket)
        ) return;

        handler.handle((SourceAudioPacket) event.getPacket());
    }

    @EventSubscribe
    public void onAudioEndPacket(@NotNull TcpPacketReceivedEvent event) {
        if (handler == null ||
                !(event.getPacket() instanceof SourceAudioEndPacket)
        ) return;

        handler.handle((SourceAudioEndPacket) event.getPacket());
    }
}
