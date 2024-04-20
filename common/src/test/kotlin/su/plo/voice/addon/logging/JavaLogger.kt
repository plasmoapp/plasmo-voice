package su.plo.voice.addon.logging

import su.plo.slib.api.logging.McLogger
import java.util.logging.Level
import java.util.logging.Logger

class JavaLogger(
    name: String
) : Logger(name, null), McLogger {

    override fun trace(format: String, vararg arguments: Any?) {
        log(Level.FINEST, String.format(format, *arguments))
    }

    override fun debug(format: String, vararg arguments: Any?) {
        log(Level.ALL, String.format(format, *arguments))
    }

    override fun info(format: String, vararg arguments: Any?) {
        log(Level.INFO, String.format(format, *arguments))
    }

    override fun warn(format: String, vararg arguments: Any?) {
        log(Level.WARNING, String.format(format, *arguments))
    }

    override fun error(format: String, vararg arguments: Any?) {
        log(Level.SEVERE, String.format(format, *arguments))
    }
}
