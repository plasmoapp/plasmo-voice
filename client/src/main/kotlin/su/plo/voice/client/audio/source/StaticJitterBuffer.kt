package su.plo.voice.client.audio.source

import su.plo.voice.BaseVoice
import su.plo.voice.api.client.time.TimeSupplier
import su.plo.voice.client.extension.nanosToMillis
import su.plo.voice.proto.packets.tcp.clientbound.SourceAudioEndPacket
import su.plo.voice.proto.packets.udp.clientbound.SourceAudioPacket
import java.util.concurrent.PriorityBlockingQueue

class StaticJitterBuffer(
    private val timeSupplier: TimeSupplier,
    packetDelay: Int,
    private val staleThresholdMillis: Long = 500,
    private val maxQueueSize: Int = 100,
    private val maxPlcPackets: Int = 5,
    private val staleHoleMillis: Long = 30,
    private val streamRestartGapMillis: Long = 200,
) : JitterBuffer {
    private val delayMillis: Long = packetDelay * 20L

    private val queue: PriorityBlockingQueue<JitterBuffer.PacketWithSequenceNumber> =
        PriorityBlockingQueue(maxOf(packetDelay * 2, 11), compareBy { it.sequenceNumber })

    @Volatile private var endPacket: SourceAudioEndPacket? = null
    @Volatile private var anchorTime: Long? = null
    @Volatile private var anchorSequenceNumber: Long = -1
    @Volatile private var lastSequenceNumber: Long = -1
    @Volatile private var lastPlcTime: Long? = null
    @Volatile private var consecutivePlcCount: Int = 0
    @Volatile private var lastArrivalTime: Long? = null

    override fun offer(packet: SourceAudioPacket, arrivalTimeMillis: Long) {
        if (endPacket != null && packet.sequenceNumber > endPacket!!.sequenceNumber) {
            reset()
        }

        if (queue.size >= maxQueueSize) {
            BaseVoice.DEBUG_LOGGER.warn(
                "Jitter buffer overflow (sourceId={}, sequenceNumber={})",
                packet.sourceId,
                packet.sequenceNumber,
            )
            return
        }

        val previousArrivalTime = lastArrivalTime
        lastArrivalTime = arrivalTimeMillis

        if (anchorTime == null) {
            anchorTime = arrivalTimeMillis
            anchorSequenceNumber = packet.sequenceNumber
        } else if (
            previousArrivalTime != null &&
            arrivalTimeMillis - previousArrivalTime >= streamRestartGapMillis &&
            packet.sequenceNumber > lastSequenceNumber &&
            queue.isEmpty()
        ) {
            reanchor(arrivalTimeMillis, packet.sequenceNumber)
        }

        queue.offer(JitterBuffer.SourceAudioPacketWrapper(packet, arrivalTimeMillis))
    }

    override fun offer(packet: SourceAudioEndPacket, arrivalTimeMillis: Long) {
        if (lastSequenceNumber >= packet.sequenceNumber) return

        endPacket = packet
        queue.offer(JitterBuffer.SourceAudioEndPacketWrapper(packet, arrivalTimeMillis))
    }

    override fun poll(): JitterBuffer.PacketWithSequenceNumber? {
        val now = timeSupplier.nanoTime.nanosToMillis()

        while (true) {
            val head = queue.peek() ?: return null

            if (now - head.arrivalTimeMillis >= staleThresholdMillis) {
                queue.poll()
                continue
            }

            val anchorTime = anchorTime

            if (lastSequenceNumber >= 0 && head.sequenceNumber > lastSequenceNumber + 1 && anchorTime != null) {
                val missingSequenceNumber = lastSequenceNumber + 1
                val missingScheduledTime = scheduledTime(anchorTime, missingSequenceNumber)

                if (now >= missingScheduledTime) {
                    if (isStale(missingScheduledTime, now)) {
                        reanchor(head.arrivalTimeMillis, head.sequenceNumber)
                        continue
                    }

                    if (consecutivePlcCount >= maxPlcPackets) {
                        // the gap is too long to conceal: filling it frame by frame only
                        // produces artifacts and keeps falling further behind real time,
                        // so drop the hole and re-anchor on the head packet
                        reanchor(now - delayMillis, head.sequenceNumber)
                    } else if (lastPlcTime.let { it == null || now >= it + 20L }) {
                        lastSequenceNumber = missingSequenceNumber
                        lastPlcTime = now
                        consecutivePlcCount++
                        return JitterBuffer.PacketLost(missingSequenceNumber, now)
                    } else {
                        return null
                    }
                } else {
                    return null
                }
            }

            val endSequenceNumber = endPacket?.sequenceNumber
            val drainingTail = endSequenceNumber != null && head.sequenceNumber <= endSequenceNumber

            if (drainingTail || (anchorTime != null && now >= scheduledTime(anchorTime, head.sequenceNumber))) {
                if (head.sequenceNumber > lastSequenceNumber) {
                    lastSequenceNumber = head.sequenceNumber
                }

                lastPlcTime = null
                consecutivePlcCount = 0

                val polled = queue.poll()
                if (polled is JitterBuffer.SourceAudioEndPacketWrapper) {
                    endStream(polled.sequenceNumber)
                }
                return polled
            }

            return null
        }
    }

    private fun endStream(endSequenceNumber: Long) {
        endPacket = null
        queue.removeIf { it.sequenceNumber <= endSequenceNumber }

        val next = queue.peek()
        if (next == null) {
            reset()
        } else {
            reanchor(next.arrivalTimeMillis, next.sequenceNumber)
        }
    }

    private fun scheduledTime(anchorTime: Long, sequenceNumber: Long): Long =
        anchorTime + (sequenceNumber - anchorSequenceNumber) * 20L + delayMillis

    private fun isStale(scheduledTime: Long, now: Long): Boolean =
        now >= scheduledTime + staleHoleMillis

    private fun reanchor(time: Long, sequenceNumber: Long) {
        anchorTime = time
        anchorSequenceNumber = sequenceNumber
        lastSequenceNumber = sequenceNumber - 1
        lastPlcTime = null
        consecutivePlcCount = 0
    }

    override fun isEmpty(): Boolean = queue.isEmpty()

    override fun reset() {
        endPacket = null
        anchorTime = null
        anchorSequenceNumber = -1
        lastSequenceNumber = -1
        lastPlcTime = null
        consecutivePlcCount = 0
        lastArrivalTime = null
    }

    override fun clear() {
        reset()
        queue.clear()
    }
}
