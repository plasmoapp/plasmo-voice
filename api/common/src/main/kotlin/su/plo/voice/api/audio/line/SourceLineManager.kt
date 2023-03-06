package su.plo.voice.api.audio.line

import su.plo.voice.proto.data.audio.line.SourceLine
import java.util.*

interface SourceLineManager<T : SourceLine> {

    val lines: Collection<T>

    fun getLineById(id: UUID): Optional<T>

    fun getLineByName(name: String): Optional<T>

    fun unregister(id: UUID): Boolean

    fun unregister(name: String): Boolean

    fun unregister(line: T): Boolean = unregister(line.id)

    fun clear()
}
