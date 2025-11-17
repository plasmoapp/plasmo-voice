package su.plo.voice.event

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import su.plo.voice.addon.TestAddonKt
import su.plo.voice.api.event.EventBus
import su.plo.voice.api.event.EventHandler
import su.plo.voice.api.event.EventPriority
import java.util.concurrent.Executors
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestEventBus {
    private val addon = Any()

    private lateinit var bus: EventBus
    private lateinit var calls: MutableList<String>

    @BeforeEach
    fun setUp() {
        bus = VoiceEventBus(Executors.newSingleThreadExecutor())
        calls = mutableListOf()
    }

    private fun recording(name: String) =
        EventHandler<TestEvent> { calls.add(name) }

    @Test
    fun firesNothingWithoutListeners() {
        assertFalse(bus.hasListener(TestEvent::class.java))
        assertTrue(bus.fire(TestEvent()))
        assertEquals(emptyList(), calls)
    }

    @Test
    fun firesInPriorityOrder() {
        bus.register(addon, TestEvent::class.java, EventPriority.HIGHEST, recording("highest"))
        bus.register(addon, TestEvent::class.java, EventPriority.LOWEST, recording("lowest"))
        bus.register(addon, TestEvent::class.java, EventPriority.HIGH, recording("high"))
        bus.register(addon, TestEvent::class.java, EventPriority.NORMAL, recording("normal"))
        bus.register(addon, TestEvent::class.java, EventPriority.LOW, recording("low"))

        assertTrue(bus.fire(TestEvent()))
        assertEquals(listOf("lowest", "low", "normal", "high", "highest"), calls)
    }

    @Test
    fun keepsRegistrationOrderWithinPriority() {
        bus.register(addon, TestEvent::class.java, EventPriority.NORMAL, recording("first"))
        bus.register(addon, TestEvent::class.java, EventPriority.LOWEST, recording("lowest"))
        bus.register(addon, TestEvent::class.java, EventPriority.NORMAL, recording("second"))
        bus.register(addon, TestEvent::class.java, EventPriority.NORMAL, recording("third"))

        bus.fire(TestEvent())
        assertEquals(listOf("lowest", "first", "second", "third"), calls)
    }

    @Test
    fun annotatedListenerRespectsPriority() {
        val listener = TestEventListener()
        bus.register(addon, listener)

        bus.fire(TestEvent())
        assertEquals(listOf("normal", "highest"), listener.calls)
    }

    @Test
    fun unregisterHandlerKeepsTheRest() {
        val removed = recording("removed")
        bus.register(addon, TestEvent::class.java, EventPriority.LOWEST, recording("kept-lowest"))
        bus.register(addon, TestEvent::class.java, EventPriority.NORMAL, removed)
        bus.register(addon, TestEvent::class.java, EventPriority.HIGHEST, recording("kept-highest"))

        bus.unregister(addon, removed)

        bus.fire(TestEvent())
        assertEquals(listOf("kept-lowest", "kept-highest"), calls)
        assertTrue(bus.hasListener(TestEvent::class.java))
    }

    @Test
    fun unregisterLastHandlerDropsEventClass() {
        val handler = recording("only")
        bus.register(addon, TestEvent::class.java, EventPriority.NORMAL, handler)
        assertTrue(bus.hasListener(TestEvent::class.java))

        bus.unregister(addon, handler)

        assertFalse(bus.hasListener(TestEvent::class.java))
        assertTrue(bus.fire(TestEvent()))
        assertEquals(emptyList(), calls)
    }

    @Test
    fun unregisterAddonRemovesListenersAndHandlers() {
        val listener = TestEventListener()
        bus.register(addon, listener)
        bus.register(addon, TestEvent::class.java, EventPriority.NORMAL, recording("handler"))
        assertTrue(bus.hasListener(TestEvent::class.java))

        bus.unregister(addon)

        assertFalse(bus.hasListener(TestEvent::class.java))
        bus.fire(TestEvent())
        assertEquals(emptyList(), calls)
        assertEquals(emptyList(), listener.calls)
    }
}
