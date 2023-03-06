package su.plo.voice.api.server.audio.capture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.lib.api.server.permission.PermissionDefault;
import su.plo.lib.api.server.permission.PermissionsManager;
import su.plo.lib.api.server.player.MinecraftServerPlayer;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.codec.CodecInfo;

import java.util.Collection;
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
    @NotNull Collection<String> getPermissions();

    /**
     * Adds a permission to the activation
     */
    void addPermission(@NotNull String permission);

    /**
     * Removes a permission from the activation
     */
    void removePermission(@NotNull String permission);

    /**
     * Clears all permissions from the activation
     */
    void clearPermissions();

    /**
     * Checks if player has any permission
     */
    boolean checkPermissions(@NotNull VoicePlayer player);

    /**
     * Checks if player has any permission
     */
    boolean checkPermissions(@NotNull MinecraftServerPlayer serverPlayer);

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
         * Adds a permission to the activation
         */
        @NotNull Builder addPermission(@NotNull String permission);

        /**
         * Sets permission default that will be used for permissions registration
         *
         * <p>
         *     Set this to null, if you want to register permissions by yourself in {@link PermissionsManager}
         * </p>
         * <p>
         *     All permissions will be automatically unregistered in {@link PermissionsManager}
         *     after unregistering activation in {@link ServerActivationManager}. This cannot be disabled
         * </p>
         *
         * <p>
         *     Default: {@link PermissionDefault#OP}
         * </p>
         */
        @NotNull Builder setPermissionDefault(@Nullable PermissionDefault permissionDefault);

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
         * Sets the activation's encoder info
         *
         * <p>
         *     If not null, client will use this info to encode audio data,
         *     otherwise server's encoder will be used
         * </p>
         * <p>
         *     Default: null
         * </p>
         */
        @NotNull Builder setEncoderInfo(@Nullable CodecInfo encoderInfo);

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
