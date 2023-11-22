package su.plo.voice.api.server.audio.capture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.plo.slib.api.entity.player.McPlayer;
import su.plo.slib.api.permission.PermissionDefault;
import su.plo.slib.api.permission.PermissionManager;
import su.plo.voice.api.addon.AddonContainer;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEndEvent;
import su.plo.voice.api.server.event.audio.source.PlayerSpeakEvent;
import su.plo.voice.api.server.player.VoicePlayer;
import su.plo.voice.proto.data.audio.capture.Activation;
import su.plo.voice.proto.data.audio.codec.CodecInfo;
import su.plo.voice.proto.packets.tcp.serverbound.PlayerAudioEndPacket;
import su.plo.voice.proto.packets.udp.serverbound.PlayerAudioPacket;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Represents a server activation.
 */
public interface ServerActivation extends Activation {

    /**
     * Gets the addon associated with the activation.
     *
     * @return The activation's addon.
     */
    @NotNull AddonContainer getAddon();

    /**
     * Gets the optional requirements associated with the activation.
     *
     * @return An optional containing the activation's requirements.
     */
    @NotNull Optional<Requirements> getRequirements();

    /**
     * Gets the permissions associated with the activation.
     *
     * @return A collection of permissions required for the activation.
     */
    @NotNull Collection<String> getPermissions();

    /**
     * Adds a permission to the activation.
     *
     * @param permission The permission to add.
     */
    void addPermission(@NotNull String permission);

    /**
     * Removes a permission from the activation.
     *
     * @param permission The permission to remove.
     */
    void removePermission(@NotNull String permission);

    /**
     * Clears all permissions.
     */
    void clearPermissions();

    /**
     * Checks if a voice player has any of the required permissions for the activation.
     *
     * @param player The voice player to check.
     * @return {@code true} if the player has the required permissions, {@code false} otherwise.
     */
    default boolean checkPermissions(@NotNull VoicePlayer player) {
        return checkPermissions(player.getInstance());
    }

    /**
     * Checks if a player has any of the required permissions for the activation.
     *
     * @param serverPlayer The player to check.
     * @return {@code true} if the player has the required permissions, {@code false} otherwise.
     */
    boolean checkPermissions(@NotNull McPlayer serverPlayer);

    /**
     * Sets the available distances for the activation.
     *
     * @param distances The list of distances.
     */
    void setDistances(@NotNull List<Integer> distances);

    /**
     * Checks if a specified distance is among the available distances for the activation.
     *
     * <p>
     *     If distances are empty, returns {@code true}.
     * </p>
     * <p>
     *     If distances are dynamic, returns {@code true} if the specified distance is within the range (0, {@link #getMaxDistance()}].
     * </p>
     *
     * @param distance The distance to check.
     * @return {@code true} if the specified distance is among the available distances, {@code false} otherwise.
     */
    boolean checkDistance(int distance);

    /**
     * Sets whether the activation is transitive.
     *
     * @param transitive Whether the activation is transitive.
     */
    void setTransitive(boolean transitive);

    /**
     * Sets whether the activation has proximity output.
     *
     * @param proximity Whether the activation has proximity output.
     */
    void setProximity(boolean proximity);

    /**
     * Invoked when a player uses the activation and meets all requirements:
     * <ul>
     *     <li>{@link #checkPermissions(VoicePlayer)}</li>
     *     <li>{@link #checkDistance(int)}</li>
     *     <li>{@link #getRequirements()}</li>
     * </ul>
     *
     * @param activationListener The listener to handle the activation event.
     */
    void onPlayerActivation(@NotNull PlayerActivationListener activationListener);

    /**
     * Invoked when a player starts using the activation and meets all requirements:
     * <ul>
     *     <li>{@link #checkPermissions(VoicePlayer)}</li>
     *     <li>{@link #checkDistance(int)}</li>
     *     <li>{@link #getRequirements()}</li>
     * </ul>
     *
     * @param activationStartListener The listener to handle the activation start event.
     */
    void onPlayerActivationStart(@NotNull ServerActivation.PlayerActivationStartListener activationStartListener);

    /**
     * Invoked when a player sends {@link PlayerAudioEndPacket} and meets all requirements:
     * <ul>
     *     <li>{@link #checkPermissions(VoicePlayer)}</li>
     *     <li>{@link #checkDistance(int)}</li>
     *     <li>{@link #getRequirements()}</li>
     *     <li>activation is in {@link VoicePlayer#getActiveActivations()}</li>
     * </ul>
     *
     * @param activationEndListener The listener to handle the activation end event.
     */
    void onPlayerActivationEnd(@NotNull ServerActivation.PlayerActivationEndListener activationEndListener);

    @FunctionalInterface
    interface PlayerActivationListener {

        /**
         * @return {@link Result#HANDLED} if {@link PlayerSpeakEvent} should be canceled.
         */
        @NotNull ServerActivation.Result onActivation(@NotNull VoicePlayer player, @NotNull PlayerAudioPacket packet);
    }

    @FunctionalInterface
    interface PlayerActivationStartListener {

        void onActivationStart(@NotNull VoicePlayer player);
    }

    @FunctionalInterface
    interface PlayerActivationEndListener {

        /**
         * @return {@link Result#HANDLED} if {@link PlayerSpeakEndEvent} should be canceled.
         */
        @NotNull ServerActivation.Result onActivationEnd(@NotNull VoicePlayer player, @NotNull PlayerAudioEndPacket packet);
    }

    /**
     * Result for activation event handlers.
     */
    enum Result {
        IGNORED,
        HANDLED
    }

    /**
     * Interface for defining activation requirements.
     */
    interface Requirements {

        /**
         * Checks if the specified player and audio packet meet the activation requirements.
         *
         * @param player The voice player.
         * @param packet The audio packet.
         * @return {@code true} if the requirements are met, {@code false} otherwise.
         */
        boolean checkRequirements(@NotNull VoicePlayer player, @NotNull PlayerAudioPacket packet);

        /**
         * Checks if the specified player and audio end packet meet the activation requirements.
         *
         * @param player The voice player.
         * @param packet The audio end packet.
         * @return {@code true} if the requirements are met, {@code false} otherwise.
         */
        boolean checkRequirements(@NotNull VoicePlayer player, @NotNull PlayerAudioEndPacket packet);
    }

    /**
     * Builder interface for creating server activations.
     */
    interface Builder {

        /**
         * Adds a permission to the activation.
         *
         * @param permission The permission to add.
         * @return The builder instance.
         */
        @NotNull Builder addPermission(@NotNull String permission);

        /**
         * Sets the default permission level to be used for permissions registration.
         *
         * <p>
         *     Set this to null if you want to register permissions manually in {@link PermissionManager}.
         * </p>
         * <p>
         *     All permissions will be automatically unregistered in {@link PermissionManager} after unregistering the activation in {@link ServerActivationManager}.
         * </p>
         *
         * <p>
         *     Default: {@link PermissionDefault#OP}
         * </p>
         *
         * @param permissionDefault The default permission level.
         * @return The builder instance.
         */
        @NotNull Builder setPermissionDefault(@Nullable PermissionDefault permissionDefault);

        /**
         * Sets the requirements for the activation.
         *
         * <p>
         *     If player or packet doesn't meet requirements, activation listeners won't be fired.
         * </p>
         *
         * <p>
         *     Default: {@code null}
         * </p>
         *
         * @param requirements The activation requirements.
         * @return The builder instance.
         */
        @NotNull Builder setRequirements(@Nullable ServerActivation.Requirements requirements);

        /**
         * Sets the available distances for the activation.
         *
         * <p>
         *     Default: <b>empty list</b>
         * </p>
         *
         * @param distances The list of distances.
         * @return The builder instance.
         */
        @NotNull Builder setDistances(@NotNull List<Integer> distances);

        /**
         * Sets the default distance for the activation.
         *
         * <p>
         *     Default: {@code 0}
         * </p>
         *
         * @param defaultDistance The default activation distance.
         * @return The builder instance.
         */
        @NotNull Builder setDefaultDistance(int defaultDistance);


        /**
         * Sets whether the activation is transitive.
         *
         * <p>
         *     If an activation is NOT transitive, subsequent activations will NOT be triggered.
         * </p>
         *
         * <p>
         *     Default: {@code true}
         * </p>
         *
         * @param transitive Whether the activation is transitive.
         * @return The builder instance.
         * @see Activation#isTransitive()
         */
        @NotNull Builder setTransitive(boolean transitive);

        /**
         * Sets whether the activation has proximity output.
         *
         * <p>
         *     This can be used by addons to create unique behavior.
         * </p>
         *
         * <p>
         *     For example in <a href="https://github.com/plasmoapp/pv-addon-soundphysics">pv-addon-soundphysics</a>
         *     it's used to create reverb from your capture only for activations with proximity output.
         * </p>
         *
         * <p>
         *     Default: {@code true}
         * </p>
         *
         * @param proximity true if the activation has proximity output.
         * @return The builder instance.
         * @see Activation#isProximity()
         */
        @NotNull Builder setProximity(boolean proximity);

        /**
         * Sets whether the activation supports stereo audio.
         *
         * <p>
         *     If enabled, the client will send stereo audio if it's configured in the client settings.
         * </p>
         *
         * <p>
         *     Default: {@code false}
         * </p>
         *
         * @param stereoSupported true if stereo audio is supported.
         * @return The builder instance.
         * @see Activation#isStereoSupported()
         */
        @NotNull Builder setStereoSupported(boolean stereoSupported);

        /**
         * Sets the encoder information for the activation.
         *
         * <p>
         *     If not null, the client will use this information to encode audio data; otherwise, the server's encoder will be used.
         * </p>
         *
         * <p>
         *     Default: {@code null}
         * </p>
         *
         * @param encoderInfo The encoder information.
         * @return The builder instance.
         * @see Activation#getEncoderInfo()
         */
        @NotNull Builder setEncoderInfo(@Nullable CodecInfo encoderInfo);

        /**
         * Builds the server activation.
         *
         * @return The created server activation.
         */
        @NotNull ServerActivation build();
    }
}
