package su.plo.voice.util.version

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SemanticVersionTest {

    @Test
    fun parsesBranch() {
        assertEquals(SemanticVersion.Branch.RELEASE, SemanticVersion.parse("spigot-2.1.17").branch())
        assertEquals(SemanticVersion.Branch.BETA, SemanticVersion.parse("fabric-1.21.1-2.1.17-beta.2").branch())
        assertEquals(SemanticVersion.Branch.ALPHA, SemanticVersion.parse("2.1.17+abcdef0-SNAPSHOT").branch())
        assertEquals(SemanticVersion.Branch.ALPHA, SemanticVersion.parse("2.1.17-beta.2+abcdef0-SNAPSHOT").branch())
    }

    @Test
    fun parsesBetaNumber() {
        assertEquals(2, SemanticVersion.parse("fabric-1.21.1-2.1.17-beta.2").betaNumber())
        assertEquals(0, SemanticVersion.parse("2.1.17-beta").betaNumber())
        assertEquals(0, SemanticVersion.parse("2.1.17").betaNumber())
    }

    @Test
    fun ordersWithinSameVersion() {
        assertOlder("2.1.17+abcdef0", "2.1.17-beta.1")
        assertOlder("2.1.17-beta.1", "2.1.17-beta.2")
        assertOlder("2.1.17-beta.9", "2.1.17-beta.10")
        assertOlder("2.1.17-beta.2", "2.1.17")
        assertOlder("2.1.16", "2.1.17-beta.1")
    }

    private fun assertOlder(older: String, newer: String) {
        val olderVersion = SemanticVersion.parse(older)
        val newerVersion = SemanticVersion.parse(newer)

        assertTrue(olderVersion.isOutdated(newerVersion), "$older should be older than $newer")
        assertFalse(newerVersion.isOutdated(olderVersion), "$newer should not be older than $older")
    }
}
