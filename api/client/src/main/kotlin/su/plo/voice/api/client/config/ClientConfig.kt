package su.plo.voice.api.client.config

import su.plo.config.entry.*
import su.plo.voice.api.client.config.overlay.OverlayPosition
import su.plo.voice.api.client.config.overlay.OverlaySourceState
import su.plo.voice.api.client.config.overlay.OverlayStyle
import java.util.UUID

/**
 * Client configuration for Plasmo Voice.
 */
interface ClientConfig {

    val debug: BooleanConfigEntry

    val disableCrowdin: BooleanConfigEntry

    val checkForUpdates: BooleanConfigEntry

    val voice: Voice

    val advanced: Advanced

    interface Voice {

        val disabled: BooleanConfigEntry

        val microphoneDisabled: BooleanConfigEntry

        val activationThreshold: DoubleConfigEntry

        val inputDevice: ConfigEntry<String>

        val outputDevice: ConfigEntry<String>

        val disableInputDevice: BooleanConfigEntry

        @Deprecated("Superseded by inputBackend", ReplaceWith("inputBackend"))
        val useJavaxInput: BooleanConfigEntry

        val inputBackend: EnumConfigEntry<InputBackend>

        val microphoneVolume: DoubleConfigEntry

        val noiseSuppression: BooleanConfigEntry

        val volume: DoubleConfigEntry

        /**
         * Can be disabled by [ConfigEntry.setDisabled].
         */
        val soundOcclusion: BooleanConfigEntry

        val directionalSources: BooleanConfigEntry

        val hrtf: BooleanConfigEntry

        val stereoCapture: BooleanConfigEntry

        val volumes: Volumes

        /**
         * Per-line volume and mute settings.
         *
         * Each entry is keyed by a line name. Per-player volumes are stored under
         * the `"source_<playerId>"` line name; the `player*` methods are shorthands
         * that build this key for you, so e.g. [getPlayerVolume] is equivalent to
         * `getVolume("source_" + playerId)`.
         */
        interface Volumes {

            /**
             * Sets the volume for the given player.
             *
             * Shorthand for [setVolume] with the `"source_<playerId>"` line name.
             *
             * @param volume in range `0.0..2.0`, where `1.0` is the original volume.
             */
            fun setPlayerVolume(playerId: UUID, volume: Double)

            /**
             * Returns the volume entry for the given player, creating it with the
             * default value if absent.
             *
             * Shorthand for [getVolume] with the `"source_<playerId>"` line name.
             */
            fun getPlayerVolume(playerId: UUID): DoubleConfigEntry

            /**
             * Mutes or unmutes the given player.
             *
             * Shorthand for [setMute] with the `"source_<playerId>"` line name.
             */
            fun setPlayerMute(playerId: UUID, muted: Boolean)

            /**
             * Returns the mute entry for the given player, creating it with the
             * default value if absent.
             *
             * Shorthand for [getMute] with the `"source_<playerId>"` line name.
             */
            fun getPlayerMute(playerId: UUID): BooleanConfigEntry

            /**
             * Sets the volume for the given line.
             *
             * @param volume in range `0.0..2.0`, where `1.0` is the original volume.
             */
            fun setVolume(lineName: String, volume: Double)

            /**
             * Returns the volume entry for the given line, creating it with the
             * default value if absent.
             */
            fun getVolume(lineName: String): DoubleConfigEntry

            /**
             * Returns `true` if a volume entry exists for the given line.
             */
            fun hasVolume(lineName: String): Boolean

            /**
             * Mutes or unmutes the given line.
             */
            fun setMute(lineName: String, muted: Boolean)

            /**
             * Returns the mute entry for the given line, creating it with the
             * default value if absent.
             */
            fun getMute(lineName: String): BooleanConfigEntry
        }
    }

    interface Advanced {

        val visualizeVoiceDistance: BooleanConfigEntry

        val visualizeVoiceDistanceOnJoin: BooleanConfigEntry

        val directionalSourcesAngle: IntConfigEntry

        val stereoSourcesToMono: BooleanConfigEntry

        val panning: BooleanConfigEntry

        val sourceTypesOverlap: EnumConfigEntry<OverlappingSourceTypes>

        val cameraSoundListener: BooleanConfigEntry

        val exponentialVolumeSlider: BooleanConfigEntry

        val exponentialDistanceGain: BooleanConfigEntry

        val jitterPacketDelay: IntConfigEntry

        val adaptiveJitterBuffer: BooleanConfigEntry

        val alPlaybackBuffers: IntConfigEntry
    }

    interface Overlay {

        val showActivationIcon: BooleanConfigEntry

        val activationIconPosition: EnumConfigEntry<IconPosition>

        val showSourceIcons: IntConfigEntry

        val showStaticSourceIcons: BooleanConfigEntry

        val overlayEnabled: BooleanConfigEntry

        val overlayPosition: EnumConfigEntry<OverlayPosition>

        val overlayStyle: EnumConfigEntry<OverlayStyle>

        val sourceStates: SourceStates

        interface SourceStates {

            fun setState(lineName: String, state: OverlaySourceState)

            fun getState(lineName: String): EnumConfigEntry<OverlaySourceState>
        }
    }
}
