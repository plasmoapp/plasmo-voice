package su.plo.voice.api.addon

interface AddonInitializer {

    /**
     * Method will be invoked on addon initialization
     */
    fun onAddonInitialize()

    /**
     * Method will be invoked on addon shutdown
     */
    fun onAddonShutdown() {}
}
