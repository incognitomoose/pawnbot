package net.shelg.pawnbot

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.requests.restaction.MessageAction
import org.junit.jupiter.api.Test

class TextSenderTest {
    @Test
    fun `hardcoded censorships are applied`() {
        // Given:
        val censor = TextSender()
        val mockPromise = mock<MessageAction> { }
        val mockChannel = mock<TextChannel> { on { sendMessage(any<CharSequence>()) } doReturn mockPromise }

        // When:
        censor.sendMessage(
                text = "I'm testing to see if n" + "igga" + "s, the n" + "igg" + "er, the spices, the bodegos and the" +
                        " kiks work correctly. Pussyn" + "igge" + "rs though... Harder.",
                channel = mockChannel
        )

        // Then:
        verify(mockChannel)
                .sendMessage("I'm testing to see if n\\*\\*\\*\\*\\*, the n\\*\\*\\*\\*\\*, the s\\*\\*\\*es," +
                        " the bod\\*\\*\\*\\* and the kiks work correctly. Pussyn\\*\\*\\*\\*\\*\\* though... Harder.")
    }

}