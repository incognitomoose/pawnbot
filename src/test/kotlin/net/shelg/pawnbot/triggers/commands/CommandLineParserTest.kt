package net.shelg.pawnbot.triggers.commands

import com.nhaarman.mockitokotlin2.mock
import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CommandLineParserTest {
    @Test
    fun `testing stuff`() {
        // Given:
        val args = "the_king helps to do !!! many things lol ollol"
        val argsPattern = "{who} {what} !!! {then_what} lol [extra]"

        // When:
        val actual = CommandLineParser().parseArgs(args, argsPattern)

        // Then:
        assertEquals(4, actual.size)
        assertEquals("the_king", actual["who"])
        assertEquals("helps to do", actual["what"])
        assertEquals("many things", actual["then_what"])
        assertEquals("ollol", actual["extra"])
    }

    @Test
    fun `no args is valid if not needed`() {
        // Given:
        val args = "join"
        val argsPattern = "join"

        // When:
        val actual = CommandLineParser().parseArgs(args, argsPattern)

        // Then:
        assertEquals(0, actual.size)
    }
}