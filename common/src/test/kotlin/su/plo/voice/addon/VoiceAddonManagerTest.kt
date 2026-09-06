package su.plo.voice.addon

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import su.plo.voice.addon.inject.injectPlasmoVoiceInto
import su.plo.voice.api.PlasmoVoice
import su.plo.voice.api.addon.AddonContainer
import su.plo.voice.api.addon.AddonDependency
import su.plo.voice.api.addon.AddonInitializer
import su.plo.voice.api.addon.AddonLoaderScope
import su.plo.voice.api.addon.InjectPlasmoVoice
import su.plo.voice.api.addon.annotation.Addon
import su.plo.voice.api.event.EventSubscribe
import su.plo.voice.event.TestEvent
import su.plo.voice.event.VoiceEventBus
import java.util.Optional
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

@Addon(id = "recording-addon", name = "Recording Addon", version = "1.2.3", authors = ["test"])
class RecordingAddon : TestAbstractAddon(), AddonInitializer {
    val calls = mutableListOf<String>()

    @InjectPlasmoVoice
    lateinit var injected: PlasmoVoice

    override fun onAddonInitialize() {
        calls += "initialize"
    }

    override fun onAddonShutdown() {
        calls += "shutdown"
    }

    @EventSubscribe
    fun onTestEvent(event: TestEvent) {
        calls += "event"
    }
}

@Addon(id = "failing-addon", version = "1.0.0", authors = [])
class FailingAddon : AddonInitializer {
    var shutdownCalled = false

    override fun onAddonInitialize(): Unit = throw IllegalStateException("boom")

    override fun onAddonShutdown() {
        shutdownCalled = true
    }
}

@Addon(id = "plain-addon", version = "1.0.0", authors = [])
class PlainAddon

@Addon(id = "bad", version = "1.0.0", authors = [])
class ShortIdAddon

class NotAnAddon

class InternalAddon : AddonInitializer {
    val calls = mutableListOf<String>()

    override fun onAddonInitialize() {
        calls += "initialize"
    }

    override fun onAddonShutdown() {
        calls += "shutdown"
    }

    @EventSubscribe
    fun onTestEvent(event: TestEvent) {
        calls += "event"
    }
}

class InternalAddonContainer(private val instance: InternalAddon) : AddonContainer {
    override fun getId(): String = "internal-addon"
    override fun getName(): String = "Internal Addon"
    override fun getScope(): AddonLoaderScope = AddonLoaderScope.ANY
    override fun getVersion(): String = "1.0.0"
    override fun getAuthors(): Collection<String> = emptyList()
    override fun getDependencies(): Collection<AddonDependency> = emptyList()
    override fun getMainClass(): Class<*> = InternalAddon::class.java
    override fun getInstance(): Optional<*> = Optional.of(instance)
}

class VoiceAddonManagerTest {
    private val voice: PlasmoVoice = TestVoice

    private lateinit var executor: ExecutorService
    private lateinit var bus: VoiceEventBus
    private lateinit var addons: VoiceAddonManager
    private lateinit var injected: MutableList<AddonContainer>

    @BeforeEach
    fun setUp() {
        executor = Executors.newSingleThreadExecutor()
        bus = VoiceEventBus(executor)
        injected = mutableListOf()
        addons = VoiceAddonManager(bus) { container ->
            injected += container
            injectPlasmoVoiceInto(voice, container.instance.get())
        }
    }

    @AfterEach
    fun tearDown() {
        executor.shutdownNow()
    }

    @Test
    fun loadRegistersContainerWithAnnotationMetadata() {
        val addon = RecordingAddon()
        addons.load(addon)

        assertTrue(addons.isLoaded("recording-addon"))

        val container = addons.getAddon("recording-addon").orElse(null)
        assertNotNull(container)
        assertEquals("Recording Addon", container.name)
        assertEquals("1.2.3", container.version)
        assertEquals(listOf("test"), container.authors.toList())
        assertEquals(AddonLoaderScope.ANY, container.scope)
        assertEquals(RecordingAddon::class.java, container.mainClass)
        assertSame(addon, container.instance.get())
        assertSame(container, addons.getAddon(addon).orElse(null))
    }

    @Test
    fun loadFallsBackToIdWhenNameIsBlank() {
        addons.load(PlainAddon())

        assertEquals("plain-addon", addons.getAddon("plain-addon").get().name)
    }

    @Test
    fun loadRunsInjectorOnce() {
        val addon = RecordingAddon()
        addons.load(addon)

        assertEquals(1, injected.size)
        assertSame(addon, injected[0].instance.get())
    }

    @Test
    fun injectorPopulatesAnnotatedAndInheritedFields() {
        val addon = RecordingAddon()
        addons.load(addon)

        assertSame(voice, addon.injected)
        assertSame(voice, addon.plasmoVoiceAbstractAnnotation)
        assertSame(voice, addon.plasmoVoiceAbstractDelegate)
    }

    @Test
    fun loadDoesNotInitializeBeforeInitializeLoadedAddons() {
        val addon = RecordingAddon()
        addons.load(addon)

        assertEquals(emptyList(), addon.calls)
        assertFalse(bus.hasListener(TestEvent::class.java))
    }

    @Test
    fun initializeLoadedAddonsInitializesAndRegistersListeners() {
        val addon = RecordingAddon()
        addons.load(addon)

        addons.initializeLoadedAddons()
        assertEquals(listOf("initialize"), addon.calls)

        bus.fire(TestEvent())
        assertEquals(listOf("initialize", "event"), addon.calls)
    }

    @Test
    fun initializeLoadedAddonsIsIdempotent() {
        val addon = RecordingAddon()
        addons.load(addon)

        addons.initializeLoadedAddons()
        addons.initializeLoadedAddons()

        assertEquals(listOf("initialize"), addon.calls)
    }

    @Test
    fun loadAfterInitializationInitializesImmediately() {
        addons.initializeLoadedAddons()

        val addon = RecordingAddon()
        addons.load(addon)

        assertEquals(listOf("initialize"), addon.calls)

        bus.fire(TestEvent())
        assertEquals(listOf("initialize", "event"), addon.calls)
    }

    @Test
    fun reloadReplacesInstanceAndUnregistersPreviousListeners() {
        val first = RecordingAddon()
        addons.load(first)
        addons.initializeLoadedAddons()

        val second = RecordingAddon()
        addons.load(second)

        bus.fire(TestEvent())

        assertEquals(listOf("initialize"), first.calls)
        assertEquals(listOf("initialize", "event"), second.calls)
        assertSame(second, addons.getAddon("recording-addon").get().instance.get())
    }

    @Test
    fun unloadShutsDownAndUnregistersListeners() {
        val addon = RecordingAddon()
        addons.load(addon)
        addons.initializeLoadedAddons()

        addons.unload(addon)
        assertEquals(listOf("initialize", "shutdown"), addon.calls)

        bus.fire(TestEvent())
        assertEquals(listOf("initialize", "shutdown"), addon.calls)
        assertFalse(bus.hasListener(TestEvent::class.java))
    }

    @Test
    fun unloadIsNoOpForUninitializedAddon() {
        val addon = RecordingAddon()
        addons.load(addon)

        addons.unload(addon)

        assertEquals(emptyList(), addon.calls)
    }

    @Test
    fun reloadAfterUnloadInitializesAgain() {
        val addon = RecordingAddon()
        addons.load(addon)
        addons.initializeLoadedAddons()
        addons.unload(addon)

        val reloaded = RecordingAddon()
        addons.load(reloaded)

        assertEquals(listOf("initialize"), reloaded.calls)

        bus.fire(TestEvent())
        assertEquals(listOf("initialize", "event"), reloaded.calls)
    }

    @Test
    fun clearShutsDownEveryInitializedAddon() {
        val addon = RecordingAddon()
        val plain = PlainAddon()
        addons.load(addon)
        addons.load(plain)
        addons.initializeLoadedAddons()

        addons.clear()

        assertEquals(listOf("initialize", "shutdown"), addon.calls)
        assertFalse(bus.hasListener(TestEvent::class.java))
    }

    @Test
    fun clearKeepsAddonsLoaded() {
        val addon = RecordingAddon()
        addons.load(addon)
        addons.initializeLoadedAddons()
        addons.clear()

        assertTrue(addons.isLoaded("recording-addon"))
        assertSame(addon, addons.getAddon("recording-addon").get().instance.get())
        assertSame(addon, addons.getAddon(addon).get().instance.get())

        addons.initializeLoadedAddons()

        assertEquals(listOf("initialize", "shutdown", "initialize"), addon.calls)

        bus.fire(TestEvent())
        assertEquals(listOf("initialize", "shutdown", "initialize", "event"), addon.calls)
    }

    @Test
    fun failedInitializationLeavesAddonUnregistered() {
        val failing = FailingAddon()
        val healthy = RecordingAddon()
        addons.load(failing)
        addons.load(healthy)

        addons.initializeLoadedAddons()

        assertEquals(listOf("initialize"), healthy.calls)

        addons.unload(failing)
        assertFalse(failing.shutdownCalled)
    }

    @Test
    fun internalAddonsAreQueryable() {
        val internal = PlasmoVoiceAddon(voice, AddonLoaderScope.ANY)
        addons.loadInternalAddon(internal)

        assertTrue(addons.isLoaded("plasmovoice"))
        assertSame(internal, addons.getAddon("plasmovoice").get())
        assertSame(internal, addons.getAddon(voice).get())
    }

    @Test
    fun internalAddonsAreNeverInitializedOrShutDown() {
        val instance = InternalAddon()
        val internal = InternalAddonContainer(instance)
        addons.loadInternalAddon(internal)

        assertTrue(addons.isLoaded("internal-addon"))
        assertSame(internal, addons.getAddon("internal-addon").get())
        assertSame(internal, addons.getAddon(instance).get())

        addons.initializeLoadedAddons()
        addons.clear()

        assertEquals(emptyList(), instance.calls)
        assertFalse(bus.hasListener(TestEvent::class.java))

        bus.fire(TestEvent())
        assertEquals(emptyList(), instance.calls)
    }

    @Test
    fun loadRejectsObjectWithoutAddonAnnotation() {
        assertFailsWith<IllegalArgumentException> { addons.load(NotAnAddon()) }
    }

    @Test
    fun unloadRejectsObjectWithoutAddonAnnotation() {
        assertFailsWith<IllegalArgumentException> { addons.unload(NotAnAddon()) }
    }

    @Test
    fun loadRejectsIdThatDoesNotMatchPattern() {
        assertFailsWith<IllegalArgumentException> { addons.load(ShortIdAddon()) }
    }
}
