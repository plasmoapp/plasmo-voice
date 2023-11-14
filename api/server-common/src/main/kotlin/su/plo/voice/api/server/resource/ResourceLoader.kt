package su.plo.slib.api.resource

import java.io.IOException
import java.io.InputStream

/**
 * Represents an interface for loading resources from a jar.
 */
interface ResourceLoader {

    /**
     * Loads a resource from the specified resource path and returns it as an input stream.
     *
     * @param resourcePath The path to the resource to be loaded.
     * @return An input stream representing the loaded resource, or `null` if the resource is not found.
     * @throws IOException If an I/O error occurs during resource loading.
     */
    @Throws(IOException::class)
    fun load(resourcePath: String): InputStream?
}
