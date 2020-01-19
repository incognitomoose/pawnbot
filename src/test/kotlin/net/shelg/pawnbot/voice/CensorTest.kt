package net.shelg.pawnbot.voice

import com.nhaarman.mockitokotlin2.mock
import net.shelg.pawnbot.censoring.Censor
import net.shelg.pawnbot.censoring.CensorshipReplacementService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CensorTest {
    @Test
    fun testName() { // Given:
        val censor = Censor(CensorshipReplacementService(
                mock {
                }
        )
        )
        val offensiveText = "I'm testing to see if niggas, the nigger, the spices, the bodegos and the kiks work correctly. Pussyniggers though... Harder."
        // When:
        val censored = censor.censorText(offensiveText, mock { }, null, true)
        // Then:
        assertEquals("I'm testing to see if n\\*\\*\\*\\*s, the n\\*\\*\\*\\*\\*, the sp\\*\\*es, the bod\\*\\*\\*s and the kiks work correctly. Pussyn\\*\\*\\*\\*\\*s though... Harder.", censored)
    }
}