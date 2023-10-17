package su.plo.voice.api.audio.line

import su.plo.voice.proto.data.audio.line.SourceLine
import java.util.*

/**
 * Base interface for managing audio source lines.
 */
interface SourceLineManager<T : SourceLine> {

    /**
     * Retrieves a collection of all source lines.
     *
     * @return A collection of source lines.
     */
    val lines: Collection<T>

    /**
     * Retrieves a source line by its unique identifier.
     *
     * @param id The unique identifier of the source line.
     * @return An optional containing the source line if found, or empty if not found.
     */
    fun getLineById(id: UUID): Optional<T>

    /**
     * Retrieves a source line by its name.
     *
     * @param name The name of the source line.
     * @return An optional containing the source line if found, or empty if not found.
     */
    fun getLineByName(name: String): Optional<T>

    /**
     * Unregisters a source line by its unique identifier.
     *
     * @param id The unique identifier of the source line to unregister.
     * @return `true` if the source line was successfully unregistered, `false` if not found.
     */
    fun unregister(id: UUID): Boolean

    /**
     * Unregisters a source line by its name.
     *
     * @param name The name of the source line to unregister.
     * @return `true` if the source line was successfully unregistered, `false` if not found.
     */
    fun unregister(name: String): Boolean

    /**
     * Unregisters a source line using the provided source line instance.
     *
     * @param line The source line to unregister.
     * @return `true` if the source line was successfully unregistered, `false` if not found.
     */
    fun unregister(line: T): Boolean = unregister(line.id)

    /**
     * Clears all source lines.
     */
    fun clear()
}
