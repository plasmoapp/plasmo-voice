package su.plo.voice.addon

import su.plo.voice.BaseVoice
import su.plo.voice.util.version.ModrinthLoader
import java.io.File

object TestVoice : BaseVoice(ModrinthLoader.FABRIC) {

    override fun getConfigFolder(): File {
        TODO("Not yet implemented")
    }
}
