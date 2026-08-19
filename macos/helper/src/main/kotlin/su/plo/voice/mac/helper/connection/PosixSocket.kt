package su.plo.voice.mac.helper.connection

import kotlinx.cinterop.*
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.UnsafeIoApi
import kotlinx.io.unsafe.UnsafeBufferOperations
import platform.darwin.inet_addr
import platform.posix.*
import su.plo.voice.mac.helper.exception.ConnectionLostException

/**
 * A raw loopback socket, readable and writable.
 */
@OptIn(ExperimentalForeignApi::class, UnsafeIoApi::class)
internal class PosixSocket(private val descriptor: Int) : RawSource, RawSink {
    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        require(byteCount >= 0) { "byteCount ($byteCount) cannot be negative." }
        if (byteCount == 0L) return 0

        var result = -1L // EoS

        UnsafeBufferOperations.writeToTail(sink, 1) { array, from, to ->
            val length = minOf((to - from).toLong(), byteCount).toInt()

            /*
             * recv()
             * https://pubs.opengroup.org/onlinepubs/9699919799/functions/recv.html
             */
            val received = uninterrupted { array.usePinned { recv(descriptor, it.addressOf(from), length.convert(), 0) } }
            if (received > 0L) result = received

            if (received > 0L) received.toInt() else 0
        }

        return result
    }

    override fun write(source: Buffer, byteCount: Long) {
        var remaining = byteCount

        while (remaining > 0) {
            UnsafeBufferOperations.readFromHead(source) { array, from, to ->
                val length = minOf((to - from).toLong(), remaining).toInt()
                /*
                 * send()
                 * https://pubs.opengroup.org/onlinepubs/9699919799/functions/send.html
                 */
                val sent = uninterrupted { array.usePinned { send(descriptor, it.addressOf(from), length.convert(), 0) } }
                if (sent <= 0L) throw ConnectionLostException()

                remaining -= sent
                sent.toInt()
            }
        }
    }

    override fun flush() = Unit

    fun shutdown() {
        /*
         * shutdown()
         * https://pubs.opengroup.org/onlinepubs/9699919799/functions/shutdown.html
         */
        shutdown(descriptor, SHUT_RDWR)
    }

    override fun close() {
        /*
         * close()
         * https://pubs.opengroup.org/onlinepubs/9699919799/functions/close.html
         */
        close(descriptor)
    }
}

/**
 * A signal landing mid syscall is not the mod hanging up.
 *
 * Taking `EINTR` for the end of the stream would drop the connection, and with it the microphone,
 * for no reason at all.
 */
private inline fun uninterrupted(transfer: () -> ssize_t): ssize_t {
    while (true) {
        val result = transfer()
        if (result >= 0 || errno != EINTR) return result
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun connectToLoopback(port: Int): PosixSocket? = memScoped {
    /*
     * socket()
     * https://pubs.opengroup.org/onlinepubs/9699919799/functions/socket.html
     */
    val descriptor = socket(AF_INET, SOCK_STREAM, 0)
    if (descriptor < 0) return null

    val address = alloc<sockaddr_in>()
    address.sin_len = sizeOf<sockaddr_in>().convert()
    address.sin_family = AF_INET.convert()

    /*
     * htons()
     * https://pubs.opengroup.org/onlinepubs/9699919799/functions/htons.html
     */
    address.sin_port = posix_htons(port.toShort()).toUShort()

    /*
     * inet_addr()
     * https://man7.org/linux/man-pages/man3/inet_addr.3.html
     */
    address.sin_addr.s_addr = inet_addr("127.0.0.1")

    /*
     * connect()
     * https://pubs.opengroup.org/onlinepubs/9699919799/functions/connect.html
     */
    if (connect(descriptor, address.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()) != 0) {
        close(descriptor)
        return null
    }

    val enabled = alloc<IntVar>().apply { value = 1 }

    /*
     * setsockopt()
     * https://pubs.opengroup.org/onlinepubs/9699919799/functions/setsockopt.html
     */
    setsockopt(descriptor, IPPROTO_TCP, TCP_NODELAY, enabled.ptr, sizeOf<IntVar>().convert())

    PosixSocket(descriptor)
}
