package su.plo.voice.util.version

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ModrinthVersionTest {
    @Test
    fun suggestsLatestWhenOnlyIntermediateVersionHasChangelog() {
        val versions = versions(
            version("spigot-2.1.16", "release", ""),
            version("spigot-2.1.15", "release", "- Fixed something"),
            version("spigot-2.1.14", "release", "- Fixed other thing")
        )

        assertEquals("spigot-2.1.16.jar", findUpdateLink("2.1.14", versions))
    }

    @Test
    fun suggestsLatestInsteadOfIntermediateVersionWithChangelog() {
        val versions = versions(
            version("spigot-2.1.16", "release", "- Fixed something"),
            version("spigot-2.1.15", "release", ""),
            version("spigot-2.1.14", "release", "- Fixed other thing")
        )

        assertEquals("spigot-2.1.16.jar", findUpdateLink("2.1.14", versions))
    }

    @Test
    fun noUpdateWhenNoNewerVersionHasChangelog() {
        val versions = versions(
            version("spigot-2.1.16", "release", " \n"),
            version("spigot-2.1.15", "release", ""),
            version("spigot-2.1.14", "release", "- Fixed other thing")
        )

        assertNull(findUpdateLink("2.1.14", versions))
    }

    @Test
    fun noUpdateWhenUpToDate() {
        val versions = versions(
            version("spigot-2.1.16", "release", "- Fixed something"),
            version("spigot-2.1.15", "release", "- Fixed other thing")
        )

        assertNull(findUpdateLink("2.1.16", versions))
    }

    @Test
    fun picksHighestVersionWhenOlderLineWasPublishedLater() {
        val versions = versions(
            version("spigot-2.1.17", "release", "- Backported fix"),
            version("spigot-2.2.0", "release", ""),
            version("spigot-2.1.16", "release", "- Fixed something")
        )

        assertEquals("spigot-2.2.0.jar", findUpdateLink("2.1.16", versions))
    }

    @Test
    fun ignoresNonReleaseVersionsOnRelease() {
        val versions = versions(
            version("spigot-2.1.18+abcdef0", "alpha", "- Fixed something"),
            version("spigot-2.1.17-beta.1", "beta", "- Fixed something else"),
            version("spigot-2.1.16", "release", "- Fixed other thing")
        )

        assertNull(findUpdateLink("2.1.16", versions))
    }

    private fun findUpdateLink(currentVersion: String, versions: JsonArray): String? =
        ModrinthVersion.findUpdate(SemanticVersion.parse(currentVersion), versions)
            .map { it.downloadLink() }
            .orElse(null)

    private fun versions(vararg versions: JsonObject) =
        JsonArray().apply { versions.forEach { add(it) } }

    private fun version(versionNumber: String, versionType: String, changelog: String) =
        JsonObject().apply {
            addProperty("version_number", versionNumber)
            addProperty("version_type", versionType)
            addProperty("changelog", changelog)
            add("files", JsonArray().apply {
                add(JsonObject().apply { addProperty("url", "$versionNumber.jar") })
            })
        }
}
