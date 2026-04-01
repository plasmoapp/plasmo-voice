package su.plo.voice.client.render.level

import su.plo.voice.api.client.event.audio.capture.ClientActivationRegisteredEvent
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.client.BaseVoiceClient
import su.plo.voice.client.config.VoiceClientConfig
import su.plo.voice.proto.data.audio.capture.VoiceActivation

class DistanceVisualizeOnJoinListener(
    private val voiceClient: BaseVoiceClient,
    private val config: VoiceClientConfig,
) {
    @EventSubscribe
    fun onProximityActivationRegistered(event: ClientActivationRegisteredEvent) {
        val activation = event.activation
        if (activation.getId() != VoiceActivation.PROXIMITY_ID) return

        val isConnected: Boolean = voiceClient.udpClientManager.isConnected()

        if (!isConnected &&
            config.advanced.visualizeVoiceDistance.value() &&
            config.advanced.visualizeVoiceDistanceOnJoin.value()
        ) {
            voiceClient.getDistanceVisualizer().render(
                activation.getDistance(),
                0x00a000,
                null
            )
        }
    }
}
