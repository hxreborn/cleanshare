package eu.hxreborn.cleanshare.ui.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ReleasesTest {
    @Test
    fun `strips dash, star and dot bullets and keeps plain lines`() {
        val notes = parseNotes("- Added a status card\n* Fixed a crash\n• Changed a label\nPlain line\n\n  ")
        assertEquals(listOf("Added a status card", "Fixed a crash", "Changed a label", "Plain line"), notes)
    }

    @Test
    fun `keeps a dash that is not followed by whitespace`() {
        assertEquals(listOf("-v flag works"), parseNotes("-v flag works"))
    }

    @Test
    fun `maps each tag to its date without the v prefix`() {
        val dates = parseReleaseDates("v3.3.0 2026-09-15\nv3.2.0 2026-08-26\n")
        assertEquals(LocalDate.of(2026, 9, 15), dates["3.3.0"])
        assertEquals(LocalDate.of(2026, 8, 26), dates["3.2.0"])
    }

    @Test
    fun `version without a line has no date`() {
        assertNull(parseReleaseDates("v3.3.0 2026-09-15")["3.4.0"])
    }

    @Test
    fun `skips malformed lines`() {
        val dates = parseReleaseDates("v3.3.0\nv3.2.0 not-a-date\nv3.1.0 2026-04-25 extra\nv3.0.0 2026-03-18")
        assertEquals(mapOf("3.0.0" to LocalDate.of(2026, 3, 18)), dates)
    }
}
