package su.plo.voice.util

import com.github.jengelman.gradle.plugins.shadow.transformers.ResourceTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.TransformerContext
import org.gradle.api.file.FileTreeElement
import java.security.MessageDigest

class DeduplicatingLicenseTransformer : ResourceTransformer {
    private val licensesHashes = LinkedHashSet<String>()
    private val licenses = LinkedHashMap<String, MutableSet<ByteArray>>()

    override fun getName(): String =
        javaClass.simpleName

    override fun canTransformResource(element: FileTreeElement): Boolean =
        element.path.startsWith("META-INF/licenses", ignoreCase = true)

    override fun transform(context: TransformerContext) {
        val content = context.inputStream.readBytes()
        if (!licensesHashes.add(sha256(content))) return

        val licenses = licenses.computeIfAbsent(context.path.substringAfterLast("/")) { LinkedHashSet() }
        licenses.add(content)
    }

    override fun hasTransformedResource(): Boolean = licensesHashes.isNotEmpty()

    override fun modifyOutputStream(os: org.apache.tools.zip.ZipOutputStream, preserveFileTimestamps: Boolean) {
        if (licensesHashes.isEmpty()) return
        licenses.forEach { (key, licenses) ->
            os.putNextEntry(org.apache.tools.zip.ZipEntry("META-INF/licenses/$key"))

            licenses.forEach { license ->
                os.write(license)
                os.write("\n\n".toByteArray(Charsets.UTF_8))
            }
        }
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(bytes).joinToString("") { "%02x".format(it) }
    }
}
