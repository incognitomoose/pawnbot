package net.shelg.pawnbot

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.censoring.Censor
import org.springframework.stereotype.Service

@Service
class TextSender(private val censor: Censor) {
    fun startTyping(channel: TextChannel) {
        channel.sendTyping().complete()
    }

    fun sendMessage(text: String, channel: TextChannel, hardcodedOnly: Boolean = false, success: (Message) -> Unit = { }) {
        channel.sendMessage(censor.censorText(text, channel.guild, channel, hardcodedOnly)).queue(success)
    }
}