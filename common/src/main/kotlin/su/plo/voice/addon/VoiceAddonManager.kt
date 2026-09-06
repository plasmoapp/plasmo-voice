package su.plo.voice.addon

import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.collect.Maps
import su.plo.voice.BaseVoice
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.addon.AddonDependency
import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.AddonManager
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.event.EventBus
import java.util.Optional
import java.util.function.Consumer

private val logger = BaseVoice.createLogger(VoiceAddonManager::class.java.simpleName)

class VoiceAddonManager(
    private val eventBus: EventBus,
    private val addonInjector: Consumer<AddonContainer> = {},
) : AddonManager {
    private val addonByInstance: MutableMap<Any, AddonContainer> = Maps.newConcurrentMap()
    private val addonById: MutableMap<String, AddonContainer> = Maps.newConcurrentMap()

    private val initializedAddons: MutableSet<String> = HashSet()

    private var initialized = false

    @Synchronized
    override fun load(addonObject: Any) {
        val addonClass: Class<*> = addonObject.javaClass
        require(addonClass.isAnnotationPresent(Addon::class.java)) { "Addon object must be annotated with @Addon" }

        val addon = addonClass.getAnnotation(Addon::class.java)

        require(AddonContainer.ID_PATTERN.matcher(addon.id).matches()) {
            "An addon id must start with a lowercase letter and may contain only lowercase letters, digits, hyphens, and underscores. It should be between 4 and 32 characters long."
        }

        val addonContainer = VoiceAddon(
            addon.id,
            if (Strings.emptyToNull(addon.name) == null) addon.id else addon.name,
            addon.scope,
            addon.version,
            Lists.newArrayList(*addon.authors),
            addon.dependencies.map { dependency ->
                AddonDependency(dependency.id, dependency.optional)
            },
            addonClass
        )

        addonContainer.setInstance(addonObject)
        loadAddon(addonContainer)
    }

    @Synchronized
    override fun unload(addonObject: Any) {
        val addonClass: Class<*> = addonObject.javaClass
        require(addonClass.isAnnotationPresent(Addon::class.java)) { "Addon object must be annotated with @Addon" }

        val addon = addonClass.getAnnotation(Addon::class.java)

        if (!initializedAddons.contains(addon.id)) return

        addonById[addon.id]?.let { shutdownAddon(it) }
    }

    override fun isLoaded(id: String): Boolean =
        addonById.containsKey(id)

    override fun getAddon(id: String): Optional<AddonContainer> =
        Optional.ofNullable(addonById[id])

    override fun getAddon(instance: Any): Optional<AddonContainer> =
        Optional.ofNullable(addonByInstance[instance])

    @Synchronized
    fun initializeLoadedAddons() {
        if (initialized) return

        addonById.values
            .filterIsInstance<VoiceAddon>()
            .forEach(this::initializeAddon)

        this.initialized = true
    }

    @Synchronized
    fun clear() {
        addonById.values
            .filter { initializedAddons.contains(it.id) }
            .filterIsInstance<VoiceAddon>()
            .forEach { shutdownAddon(it) }

        this.initialized = false
    }

    @Synchronized
    fun loadInternalAddon(addon: AddonContainer) {
        addonById[addon.id] = addon
        addonByInstance[addon.instance.get()] = addon
    }

    private fun loadAddon(addon: AddonContainer) {
        if (initialized) {
            // unregister old event listeners
            addonById[addon.id]?.let { oldAddon ->
                eventBus.unregister(oldAddon.instance.get())
            }
        }

        addonInjector.accept(addon)

        addonById[addon.id] = addon
        addonByInstance[addon.instance.get()] = addon

        if (initialized) initializeAddon(addon)
    }

    private fun initializeAddon(addon: AddonContainer) {
        val addonInstance = addon.instance.get()
        if (addonInstance is AddonInitializer) {
            try {
                addonInstance.onAddonInitialize()
            } catch (e: Exception) {
                logger.warn(
                    "Failed to initialized addon {} v{} by {}",
                    addon.id,
                    addon.version,
                    addon.authors.joinToString(", "),
                    e
                )
                return
            }
        }

        eventBus.register(addonInstance, addonInstance)
        initializedAddons.add(addon.id)

        logger.info(
            "{} v{} by {} loaded",
            addon.id,
            addon.version,
            addon.authors.joinToString(", ")
        )
    }

    private fun shutdownAddon(addon: AddonContainer) {
        val addonInstance = addon.instance.get()
        if (addonInstance is AddonInitializer) {
            addonInstance.onAddonShutdown()
        }

        eventBus.unregister(addon.instance.get())
        initializedAddons.remove(addon.id)

        logger.info(
            "Addon {} v{} by {} unloaded",
            addon.id,
            addon.version,
            java.lang.String.join(", ", addon.authors)
        )
    }
}
