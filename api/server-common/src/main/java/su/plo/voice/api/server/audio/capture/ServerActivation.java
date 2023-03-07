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
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ServerActivation extends Activation {

    /**
     * Gets the activation's addon
     *
     * @return the activation's addon
     */
    @NotNull AddonContainer getAddon();

    /**
     * @return the activation's optional requirements
     */
    @NotNull Optional<Requirements> getRequirements();

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
    default boolean checkPermissions(@NotNull VoicePlayer player) {
        return checkPermissions(player.getInstance());
    }

    /**
     * Checks if player has any permission
     */
    boolean checkPermissions(@NotNull MinecraftServerPlayer serverPlayer);

    /**
     * Sets the activation's available distances
     */
    void setDistances(List<Integer> distances);

    /**
     * Checks true if specified distance is in {@link #getDistances()}
     *
     * <p>
     *     If distances are empty, returns true
     * </p>
     * <p>
     *     If distances are dynamic, returns true if specified distance is in (0, {@link #getMaxDistance()}]
     * </p>
     *
     * @return true if specified distance is in {@link #getDistances()}
     */
    boolean checkDistance(int distance);

    /**
     * Sets the activation's transitivity
     */
    void setTransitive(boolean transitive);

    /**
     * Sets the activation's proximity
     */
    void setProximity(boolean transitive);

    /**
     * Fired when the player using activation and meet all requirements:
     * <ul>
     *     <li>{@link #checkPermissions(VoicePlayer)}</li>
     *     <li>{@link #checkDistance(int)}</li>
     *     <li>{@link #getRequirements()}</li>
     * </ul>
     */
    void onPlayerActivation(@NotNull PlayerActivationListener activationListener);

    /**
     * Fired when the player starts using the activation and meet all requirements:
     * <ul>
     *     <li>{@link #checkPermissions(VoicePlayer)}</li>
     *     <li>{@link #checkDistance(int)}</li>
     *     <li>{@link #getRequirements()}</li>
     * </ul>
     */
    void onPlayerActivationStart(@NotNull ServerActivation.PlayerActivationStartListener activationStartListener);

    /**
     * Fired when the player sends {@link PlayerAudioEndPacket} and meet all requirements:
     * <ul>
     *     <li>{@link #checkPermissions(VoicePlayer)}</li>
     *     <li>{@link #checkDistance(int)}</li>
     *     <li>{@link #getRequirements()}</li>
     *     <li>activation in {@link VoicePlayer#getActiveActivations()}</li>
     * </ul>
     */
    void onPlayerActivationEnd(@NotNull ServerActivation.PlayerActivationEndListener activationEndListener);

    @FunctionalInterface
    interface PlayerActivationListener {

        void onActivation(@NotNull VoicePlayer player, @NotNull PlayerAudioPacket packet);
    }

    @FunctionalInterface
    interface PlayerActivationStartListener {

        void onActivationStart(@NotNull VoicePlayer player);
    }

    @FunctionalInterface
    interface PlayerActivationEndListener {

        void onActivationEnd(@NotNull VoicePlayer player, @NotNull PlayerAudioEndPacket packet);
    }

    interface Requirements {

        boolean checkRequirements(@NotNull VoicePlayer player, @NotNull PlayerAudioPacket packet);

        boolean checkRequirements(@NotNull VoicePlayer player, @NotNull PlayerAudioEndPacket packet);
    }

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
         * Sets the activation's requirements
         *
         * <p>
         *     If player or packet doesn't meet requirements, activation listeners won't be fired
         * </p>
         *
         * <p>
         *     By default, activation only check permissions and distances
         * </p>
         *
         * <p>
         *     Default: null
         * </p>
         */
        @NotNull Builder setRequirements(@Nullable ServerActivation.Requirements requirements);

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
