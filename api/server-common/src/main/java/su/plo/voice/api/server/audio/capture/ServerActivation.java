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
     * Gets the activation's permission
     */
    @NotNull String getPermission();

    /**
     * Sets the activation's permission
     *
     * @param permission the activation's permission
     */
    void setPermission(@NotNull String permission);

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

    interface Builder {

        /**
         * Sets the activation's available distances
         * <p>
         * Default: empty list
         */
        @NotNull Builder setDistances(@NotNull List<Integer> distances);

        /**
         * Sets the activation's default distance
         * <p>
         * Default: 0
         */
        @NotNull Builder setDefaultDistance(int defaultDistance);

        /**
         * Sets the activation's transitivity
         * <p>
         * Default: true
         */
        @NotNull Builder setTransitive(boolean transitive);

        /**
         * Sets the activation's proximity, used by client to determine proximity activations
         * <p>
         * Default: true
         */
        @NotNull Builder setProximity(boolean proximity);

        /**
         * Sets the activation's stereo support
         * <p>
         * Default: false
         */
        @NotNull Builder setStereoSupported(boolean stereoSupported);

        /**
         * Builds the activation
         */
        @NotNull ServerActivation build();
    }

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
