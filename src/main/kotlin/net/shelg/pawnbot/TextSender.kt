package net.shelg.pawnbot

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import org.springframework.stereotype.Service


@Service
class TextSender {
    private val filter = HardcodedFilter { filteredWord ->
        filteredWord.first() + "\\\\*".repeat(filteredWord.length - 1)
    }

    fun startTyping(channel: TextChannel) {
        channel.sendTyping().complete()
    }

    fun sendMessage(text: String, channel: TextChannel, success: (Message) -> Unit = { }) {
        channel.sendMessage(filter.apply(text)).queue(success)
    }
}

