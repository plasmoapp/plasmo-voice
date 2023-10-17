package su.plo.voice.api.addon

interface AddonInitializer {

    /**
     * This method will be invoked when the addon is initialized.
     */
    fun onAddonInitialize()

    /**
     * This method will be invoked when the addon is being shut down.
     */
    fun onAddonShutdown() {}
}
