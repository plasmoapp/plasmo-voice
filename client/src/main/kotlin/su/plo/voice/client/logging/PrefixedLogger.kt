package su.plo.voice.client.logging

import su.plo.slib.api.logging.McLogger

class PrefixedLogger(
    private val baseLogger: McLogger,
    private val prefixGetter: () -> String,
) : McLogger {
    private val prefix
        get() = prefixGetter()

    override fun getName(): String = baseLogger.getName()

    override fun trace(format: String, vararg arguments: Any?) {
        baseLogger.trace("[$prefix] $format", *arguments)
    }

    override fun debug(format: String, vararg arguments: Any?) {
        baseLogger.debug("[$prefix] $format", *arguments)
    }

    override fun info(format: String, vararg arguments: Any?) {
        baseLogger.info("[$prefix] $format", *arguments)
    }

    override fun warn(format: String, vararg arguments: Any?) {
        baseLogger.warn("[$prefix] $format", *arguments)
    }

    override fun error(format: String, vararg arguments: Any?) {
        baseLogger.error("[$prefix] $format", *arguments)
    }
}
