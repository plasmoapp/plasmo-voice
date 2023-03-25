package su.plo.voice.api.addon

import org.jetbrains.annotations.ApiStatus.Internal

abstract class VoiceAddonsLoader : AddonsLoader {

    private var addonManager: AddonManager? = null

    private val addonsToLoad: MutableSet<Any> = HashSet()

    @Synchronized
    override fun load(addonObject: Any) {
        addonManager?.load(addonObject) ?: run {
            // because fabric (and probably forge) initializes mods in dependency-independent order,
            // addons should be loaded with PV initialization
            addonsToLoad.add(addonObject)
        }
    }

    @Synchronized
    @Internal
    fun setAddonManager(addonManager: AddonManager) {
        this.addonManager = addonManager
        addonsToLoad.removeAll {
            addonManager.load(it)
            true
        }
    }
}
