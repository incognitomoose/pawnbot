package net.shelg.pawnbot.censoring.commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.censoring.Censor
import net.shelg.pawnbot.triggers.commands.AbstractChatSubcommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import org.springframework.stereotype.Service

@Service
class CensorTestChatSubcommand(
        cliParser: CommandLineParser,
        private val censor: Censor,
        private val textSender: TextSender
) : AbstractChatSubcommand(cliParser, textSender) {

    override fun commandSyntax() = "test {text}"

    override fun execute(args: Map<String, String>, context: Message) {
        val channel = context.textChannel
        textSender.startTyping(channel)
        val response = respond(
                text = args.getValue("text"),
                channel = channel
        )
        textSender.sendMessage(response, channel)
    }

    private fun respond(text: String, channel: TextChannel) = censor.censorText(text, channel)
}