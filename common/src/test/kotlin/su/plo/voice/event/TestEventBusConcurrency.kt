package su.plo.voice.event

import org.junit.jupiter.api.Test
import su.plo.voice.addon.TestAddonKt
import su.plo.voice.api.event.Event
import su.plo.voice.api.event.EventBus
import su.plo.voice.api.event.EventPriority
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.assertEquals

class TestEventBusConcurrency {
    private val addon = Any()

    class EventA : Event
    class EventB : Event
    class EventC : Event

    private fun runConcurrentRegistration(bus: EventBus, fired: AtomicInteger) {
        val barrier = CyclicBarrier(THREADS)

        (0 until THREADS)
            .map { threadIndex ->
                thread {
                    barrier.await()

                    for (i in 0 until PER_THREAD) {
                        bus.register(
                            addon,
                            EVENT_CLASSES[(threadIndex + i) % EVENT_CLASSES.size],
                            PRIORITIES[i % PRIORITIES.size],
                        ) { fired.incrementAndGet() }
                    }
                }
            }
            .forEach { it.join() }
    }

    @Test
    fun concurrentRegistrationLosesNoHandlers() {
        val bus = createBus()
        val fired = AtomicInteger()

        runConcurrentRegistration(bus, fired)

        bus.fire(EventA())
        bus.fire(EventB())
        bus.fire(EventC())

        assertEquals(THREADS * PER_THREAD, fired.get())
    }

    @Test
    fun concurrentRegistrationIsVisibleToConcurrentFire() {
        val bus = createBus()
        val fired = AtomicInteger()
        val firerErrors = AtomicInteger()
        val running = AtomicBoolean(true)

        val firer = thread {
            while (running.get()) {
                try {
                    bus.fire(EventA())
                    bus.fire(EventB())
                    bus.fire(EventC())
                } catch (e: Throwable) {
                    firerErrors.incrementAndGet()
                    e.printStackTrace()
                }
            }
        }

        runConcurrentRegistration(bus, fired)

        running.set(false)
        firer.join()

        fired.set(0)
        bus.fire(EventA())
        bus.fire(EventB())
        bus.fire(EventC())

        assertEquals(0, firerErrors.get(), "fire() threw while handlers were being registered")
        assertEquals(THREADS * PER_THREAD, fired.get())
    }

    private fun createBus() = VoiceEventBus(Executors.newSingleThreadExecutor())

    companion object {
        private const val THREADS = 8
        private const val PER_THREAD = 500

        @Suppress("UNCHECKED_CAST")
        private val EVENT_CLASSES = arrayOf(
            EventA::class.java as Class<Event>,
            EventB::class.java as Class<Event>,
            EventC::class.java as Class<Event>,
        )

        private val PRIORITIES = EventPriority.values()
    }
}
