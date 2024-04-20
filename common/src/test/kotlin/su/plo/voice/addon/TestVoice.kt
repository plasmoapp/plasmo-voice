package su.plo.voice.addon

import su.plo.slib.api.logging.McLoggerFactory
import su.plo.voice.BaseVoice
import su.plo.voice.addon.logging.JavaLogger
import su.plo.voice.util.version.ModrinthLoader
import java.io.File

object TestVoice : BaseVoice(ModrinthLoader.FABRIC) {

    init {
        McLoggerFactory.supplier = McLoggerFactory.Supplier { name -> JavaLogger(name) }
    }

    override fun getConfigFolder(): File {
        TODO("Not yet implemented")
    }
}
