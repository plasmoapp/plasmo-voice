package su.plo.voice.api.addon

interface AddonsLoader {

    fun load(addonObject: Any)

    fun unload(addonObject: Any)
}
