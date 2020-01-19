package net.shelg.pawnbot.formatters

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

class DurationFormatterTest {
    @Test
    fun `formats as expected`() {
        // Given:
        val duration = Duration.ofSeconds(12345L)

        // When:
        val formatted = DurationFormatter.formatDuration(duration)

        // Then:
        assertEquals("3 hours, 25 minutes and 45 seconds", formatted)
    }
}