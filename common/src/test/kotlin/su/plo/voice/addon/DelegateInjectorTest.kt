package su.plo.voice.addon

import org.junit.jupiter.api.Test

class DelegateInjectorTest {

    @Test
    fun inject() {
        val voice = TestVoice
        val addonManager = voice.addonManager as VoiceAddonManager

        val testAddon = TestAddonKt()

        addonManager.load(testAddon)
        addonManager.initializeLoadedAddons()
    }
}
