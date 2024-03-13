package su.plo.voice.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import su.plo.slib.api.logging.McLogger

class Slf4jLogger(name: String) : McLogger {

    val logger: Logger = LoggerFactory.getLogger(name)

    override fun getName(): String =
        logger.name

    override fun trace(format: String, vararg arguments: Any) {
        logger.trace(format, *arguments)
    }

    override fun debug(format: String, vararg arguments: Any) {
        logger.debug(format, *arguments)
    }

    override fun info(format: String, vararg arguments: Any) {
        logger.info(format, *arguments)
    }

    override fun warn(format: String, vararg arguments: Any) {
        logger.warn(format, *arguments)
    }

    override fun error(format: String, vararg arguments: Any) {
        logger.error(format, *arguments)
    }
}
