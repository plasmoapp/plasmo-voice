package su.plo.voice.addon

import com.google.common.collect.Maps
import su.plo.voice.BaseVoice
import su.plo.voice.api.PlasmoVoice
import su.plo.voice.util.version.ModrinthLoader
import java.io.File

object TestVoice : BaseVoice(ModrinthLoader.FABRIC) {

    override fun getConfigFolder(): File {
        TODO("Not yet implemented")
    }

    override fun createInjectModule(): MutableMap<Class<*>, Any> {
        val injectModule: MutableMap<Class<*>, Any> = Maps.newHashMap()
        injectModule[PlasmoVoice::class.java] = this
        return injectModule
    }
}
