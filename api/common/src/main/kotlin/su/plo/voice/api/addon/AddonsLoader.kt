package su.plo.voice.api.addon

import su.plo.voice.api.addon.annotation.Addon

/**
 * Addons should be annotated with [Addon].
 */
interface AddonsLoader {

    /**
     * Loads the specified addon.
     *
     * @param addonObject The addon to be loaded.
     */
    fun load(addonObject: Any)

    /**
     * Unloads the specified addon.
     *
     * @param addonObject The addon to be unloaded.
     */
    fun unload(addonObject: Any)
}
