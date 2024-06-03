package su.plo.voice.api.client.config

import su.plo.config.entry.*
import su.plo.voice.api.client.config.overlay.OverlayPosition
import su.plo.voice.api.client.config.overlay.OverlaySourceState
import su.plo.voice.api.client.config.overlay.OverlayStyle

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

        val useJavaxInput: BooleanConfigEntry

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

        interface Volumes {

            fun setVolume(lineName: String, volume: Double)

            fun getVolume(lineName: String): DoubleConfigEntry

            fun setMute(lineName: String, muted: Boolean)

            fun getMute(lineName: String): BooleanConfigEntry
        }
    }

    interface Advanced {

        val visualizeVoiceDistance: BooleanConfigEntry

        val visualizeVoiceDistanceOnJoin: BooleanConfigEntry

        val directionalSourcesAngle: IntConfigEntry

        val stereoSourcesToMono: BooleanConfigEntry

        val panning: BooleanConfigEntry

        val cameraSoundListener: BooleanConfigEntry

        val exponentialVolumeSlider: BooleanConfigEntry

        val exponentialDistanceGain: BooleanConfigEntry

        val jitterPacketDelay: IntConfigEntry

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
