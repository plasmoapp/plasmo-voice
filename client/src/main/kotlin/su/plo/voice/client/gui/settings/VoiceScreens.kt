package su.plo.voice.client.gui.settings

import su.plo.lib.mod.client.gui.screen.ScreenWrapper
import su.plo.voice.api.client.socket.UdpClient
import su.plo.voice.client.BaseVoiceClient
import java.util.*
import kotlin.jvm.optionals.getOrNull

object VoiceScreens {

    private var settingsScreen: VoiceSettingsScreen? = null

    fun openSettings(voiceClient: BaseVoiceClient) {
        val wrappedScreen = ScreenWrapper.getCurrentWrappedScreen()
        if (wrappedScreen.getOrNull()?.screen is VoiceSettingsScreen) {
            ScreenWrapper.openScreen(null)
            return
        }

        if (!voiceClient.udpClientManager.isConnected) {
            openNotAvailable(voiceClient)
            return
        }

        if (settingsScreen == null) {
            this.settingsScreen = VoiceSettingsScreen(voiceClient)
        }

        ScreenWrapper.openScreen(settingsScreen)
    }

    fun openNotAvailable(voiceClient: BaseVoiceClient) {
        val notAvailableScreen = VoiceNotAvailableScreen(voiceClient)
        val udpClient: Optional<UdpClient> = voiceClient.udpClientManager.client
        if (udpClient.isPresent) {
            if (udpClient.get().isClosed) {
                notAvailableScreen.setCannotConnect()
            } else {
                notAvailableScreen.setConnecting()
            }
        }
        ScreenWrapper.openScreen(notAvailableScreen)
    }
}
