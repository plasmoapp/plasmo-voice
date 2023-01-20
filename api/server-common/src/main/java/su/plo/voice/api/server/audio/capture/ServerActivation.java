package su.plo.voice.api.server.audio.capture;

import org.jetbrains.annotations.NotNull;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.proto.data.audio.capture.Activation;

import java.util.List;

public interface ServerActivation extends Activation {

    /**
     * Gets the activation's addon
     *
     * @return the activation's addon
     */
    @NotNull AddonContainer getAddon();

    /**
     * Sets the activation's available distances
     */
    void setDistances(List<Integer> distances);

    /**
     * Sets the activation's transitivity
     */
    void setTransitive(boolean transitive);

    /**
     * Sets the activation's proximity
     */
    void setProximity(boolean transitive);

//    /**
//     * Sets the activation's handler
//     */
//    void setHandler(@Nullable Handler handler);
//
//    interface Handler {
//
//        /**
//         * @return true if packet is handled and UdpPacketReceivedEvent should be cancelled
//         */
//        boolean handle(@NotNull SourceAudioPacket packet);
//
//        /**
//         * @return true if packet is handled and UdpPacketReceivedEvent should be cancelled
//         */
//        boolean handle(@NotNull SourceAudioEndPacket packet);
//    }
}
