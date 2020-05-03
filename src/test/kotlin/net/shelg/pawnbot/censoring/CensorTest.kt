package net.shelg.pawnbot.censoring

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CensorTest {
    private val guildId = 123L
    private var idCounter = 1L

    @Test
    fun `hardcoded censorships are applied`() {
        // Given:
        val censor = mockCensor(emptyList())

        // When:
        val censored = censor.censorTextForTest("I'm testing to see if n" + "igga" + "s, the n" + "igg" + "er," +
                " the spices, the bodegos and the kiks work correctly. Pussyn" + "igge" + "rs though... Harder.")

        // Then:
        assertEquals("I'm testing to see if n\\*\\*\\*\\*s, the n\\*\\*\\*\\*\\*, the sp\\*\\*es," +
                " the bod\\*\\*\\*s and the kiks work correctly. Pussyn\\*\\*\\*\\*\\*s though... Harder.", censored)
    }

    @Test
    fun `single censorship is applied`() {
        // Given:
        val censor = mockCensor(listOf(
                replacement("world", "universe")
        ))

        // When:
        val censored = censor.censorTextForTest("Hello world!")

        // Then:
        assertEquals("Hello universe!", censored)
    }

    @Test
    fun `partial words are censored`() {
        // Given:
        val censor = mockCensor(listOf(
                replacement("ass", "butt")
        ))

        // When:
        val censored = censor.censorTextForTest("assassin's creed")

        // Then:
        assertEquals("buttassin's creed", censored)
    }

    @Test
    fun `uppercase in source is retained`() {
        // Given:
        val censor = mockCensor(listOf(
                replacement("lol", "haha")
        ))

        // When:
        val censored = censor.censorTextForTest("What a joke! LOL!")

        // Then:
        assertEquals("What a joke! HAHA!", censored)
    }

    @Test
    fun `multiple censorships are applied`() {
        // Given:
        val censor = mockCensor(listOf(
                replacement("Hello", "Goodbye"),
                replacement("world", "universe")
        ))

        // When:
        val censored = censor.censorTextForTest("Hello world!")

        // Then:
        assertEquals("Goodbye universe!", censored)
    }

    @Test
    fun `multiple censorships are not applied to same text`() {
        // Given:
        val censor = mockCensor(listOf(
                replacement("world", "wurd"),
                replacement("wurd", "turd")
        ))

        // When:
        val censored = censor.censorTextForTest("Hello world!")

        // Then:
        assertEquals("Hello wurd!", censored)
    }

    private fun mockCensor(replacements: List<CensorshipReplacement>) =
            Censor(CensorshipReplacementService(createMockRepo(replacements)))

    private fun replacement(matchPhrase: String, replacementPhrase: String) =
            CensorshipReplacement(
                    groupKey = "test",
                    guildId = guildId,
                    id = idCounter++,
                    matchPhrase = matchPhrase,
                    replacementPhrase = replacementPhrase
            )

    private fun Censor.censorTextForTest(text: String) =
            censorText(
                    text = text,
                    guild = mock { on { idLong } doReturn guildId },
                    channel = mock(),
                    hardcodedOnly = false
            )

    private fun createMockRepo(replacements: List<CensorshipReplacement>): CensorshipReplacementRepository =
            mock {
                on { findAllByGuildId(any()) } doAnswer { invocation ->
                    val guildId = invocation.getArgument(0) as Long
                    replacements.filter { it.guildId == guildId }
                }
            }
}