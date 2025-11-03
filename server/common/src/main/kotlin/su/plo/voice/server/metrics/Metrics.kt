package su.plo.voice.server.metrics

import com.google.common.collect.Maps
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import io.micrometer.core.instrument.Timer
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadDeadlockMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.netty4.NettyAllocatorMetrics
import io.micrometer.core.instrument.binder.netty4.NettyEventExecutorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufAllocatorMetricProvider
import io.netty.util.concurrent.EventExecutor
import org.eclipse.jetty.server.Server
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import su.plo.voice.BaseVoice
import su.plo.voice.api.server.config.ServerConfig
import su.plo.voice.proto.packets.Packet
import su.plo.voice.proto.packets.udp.bothbound.PingPacket
import java.net.InetSocketAddress
import java.time.Duration
import java.util.concurrent.atomic.AtomicInteger

fun createPrometheusMetrics(config: ServerConfig.Metrics): Metrics {
    val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    if (config.jvmMetrics()) {
        ClassLoaderMetrics().bindTo(registry)
        JvmMemoryMetrics().bindTo(registry)
        JvmGcMetrics().bindTo(registry)
        ProcessorMetrics().bindTo(registry)
        JvmThreadMetrics().bindTo(registry)
        JvmThreadDeadlockMetrics().bindTo(registry)
    }

    val app = routes(
        "/metrics" bind Method.GET to {
            Response(OK)
                .header("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
                .body(registry.scrape())
        }
    )

    val jettyServer =
        if (config.ip() != "0.0.0.0") {
            Server(InetSocketAddress(config.ip(), config.port()))
        } else {
            Server(config.port())
        }
    val server = app.asServer(
        Jetty(
            port = config.port(),
            server = jettyServer,
        )
    ).start()

    BaseVoice.LOGGER.info("Prometheus exporter started on :${server.port()}")

    return Metrics(config, registry, server)
}

class Metrics(
    private val config: ServerConfig.Metrics,
    private val registry: MeterRegistry,
    private val httpServer: Http4kServer,
) {

    enum class PacketDirection(
        val direction: String,
    ) {
        In("in"),
        Out("out"),
    }

    enum class PacketKind(
        val kind: String,
    ) {
        Media("media"),
        Control("control"),
    }

    enum class PacketHandlerErrorStage(
        val stage: String,
    ) {
        Decode("decode"),
        Handle("handle"),
    }

    private val activePeerGauges = Maps.newConcurrentMap<String, AtomicInteger>()

    // pv_handler_time_seconds histo
    // buckets: 0.0005,0.001,0.002,0.005,0.01,0.02,0.05,0.1
    private val handlerTimeSeconds: Timer = Timer.builder("pv_handler_time_seconds")
        .publishPercentileHistogram(true)
        .sla(
            *listOf(0.0005, 0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1)
                .map { (it * 1e9).toLong() }
                .map { Duration.ofNanos(it) }
                .toTypedArray()
        )
        .register(registry)

    // pv_pipeline_time_seconds histo
    // buckets: 0.0005,0.001,0.002,0.005,0.01,0.02,0.05,0.1
    private val pipelineTimeSeconds: Timer = Timer.builder("pv_pipeline_time_seconds")
        .publishPercentileHistogram(true)
        .sla(
            *listOf(0.0005, 0.001, 0.002, 0.005, 0.01, 0.02, 0.05, 0.1)
                .map { (it * 1e9).toLong() }
                .map { Duration.ofNanos(it) }
                .toTypedArray()
        )
        .register(registry)

    // pv_keep_alive_lag_seconds histo
    // buckets:
    private val keepAliveLagSeconds: Timer = Timer.builder("pv_keep_alive_lag_seconds")
        .publishPercentileHistogram(true)
        .sla(
            *listOf(0.2, 0.5, 1.0, 2.0, 3.0, 5.0, 8.0, 13.0)
                .map { (it * 1e9).toLong() }
                .map { Duration.ofNanos(it) }
                .toTypedArray()
        )
        .register(registry)

    // pv_rtt_seconds histo
    private val rttSeconds: Timer = Timer.builder("pv_rtt_seconds")
        .publishPercentileHistogram(true)
        .sla(
            *listOf(0.01, 0.02, 0.05, 0.1, 0.2, 0.5, 1.0, 2.0)
                .map { (it * 1e9).toLong() }
                .map { Duration.ofNanos(it) }
                .toTypedArray()
        )
        .register(registry)

    init {
        PacketHandlerErrorStage.entries.forEach {
            Counter.builder("pv_pipeline_errors_total")
                .tag("stage", it.stage)
                .register(registry)
        }
    }

    fun recordHandlerTimeSeconds(duration: Duration) {
        handlerTimeSeconds.record(duration)
    }

    fun recordPipelineTimeSeconds(duration: Duration) {
        pipelineTimeSeconds.record(duration)
    }

    fun recordKeepAliveTimeSeconds(duration: Duration) {
        keepAliveLagSeconds.record(duration)
    }

    fun recordRtt(duration: Duration) {
        rttSeconds.record(duration)
    }

    fun attachNettyAllocatorMetrics(alloc: ByteBufAllocator) {
        if (config.jvmMetrics()) {
            NettyAllocatorMetrics(alloc as ByteBufAllocatorMetricProvider).bindTo(registry)
        }
    }

    fun attachNettyExecutorMetrics(eventExecutor: Iterable<EventExecutor>) {
        if (config.jvmMetrics()) {
            NettyEventExecutorMetrics(eventExecutor).bindTo(registry)
        }
    }

    // pv_udp_packets_total{dir="in|out",kind="media|control"}
    fun recordPacket(direction: PacketDirection, kind: PacketKind, bytes: Int) {
        registry.counter(
            "pv_udp_packets_total",
            "dir", direction.direction,
            "kind", kind.kind
        ).increment()

        registry.counter(
            "pv_udp_bytes_total",
            "dir", direction.direction,
            "kind", kind.kind
        ).increment(bytes.toDouble())
    }

    // pv_active_peers{public_ip="127.0.0.1"}
    fun gaugeActivePeer(publicIp: String, number: Int): Int {
        val gauge = activePeerGauges.computeIfAbsent(publicIp) {
            initPublicIpMetrics(publicIp)
            registry.gauge(
                "pv_active_peers",
                Tags.of("public_ip", publicIp),
                AtomicInteger(0)
            )
        }
        return gauge.addAndGet(number)
    }

    // pv_hard_timeouts_total{public_ip="127.0.0.1"}
    fun hardTimeout(publicIp: String) {
        registry.counter(
            "pv_hard_timeouts_total",
            "public_ip", publicIp,
        ).increment()
    }

    // pv_rejoin_attempts_total{public_ip="127.0.0.1"}
    fun rejoinAttempt(publicIp: String) {
        registry.counter(
            "pv_rejoin_attempts_total",
            "public_ip", publicIp,
        ).increment()
    }

    // pv_rejoin_success_total{public_ip="127.0.0.1"}
    fun rejoinSuccess(publicIp: String) {
        registry.counter(
            "pv_rejoin_success_total",
            "public_ip", publicIp,
        ).increment()
    }

    // pv_pipeline_errors_total{stage="decode|handle"}
    fun handlerError(stage: PacketHandlerErrorStage) {
        registry.counter(
            "pv_pipeline_errors_total",
            "stage", stage.stage,
        ).increment()
    }

    fun stop() {
        httpServer.stop()
    }

    private fun initPublicIpMetrics(publicIp: String) {
        registry.counter(
            "pv_hard_timeouts_total",
            "public_ip", publicIp,
        )

        registry.counter(
            "pv_rejoin_attempts_total",
            "public_ip", publicIp,
        )

        registry.counter(
            "pv_rejoin_success_total",
            "public_ip", publicIp,
        )
    }
}

internal fun getPacketKind(packet: Packet<*>): Metrics.PacketKind =
    if (packet is PingPacket) {
        Metrics.PacketKind.Control
    } else {
        Metrics.PacketKind.Media
    }
