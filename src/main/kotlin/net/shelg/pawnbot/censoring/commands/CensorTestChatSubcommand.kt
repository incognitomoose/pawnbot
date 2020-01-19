package net.shelg.pawnbot.censoring.commands

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.triggers.commands.AbstractChatSubcommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import org.springframework.stereotype.Service

@Service
class CensorTestChatSubcommand(
        cliParser: CommandLineParser,
        private val textSender: TextSender
) : AbstractChatSubcommand(cliParser, textSender) {

    override fun commandSyntax() = "test {text}"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = args.getValue("text")
        textSender.sendMessage(response, context.textChannel)
    }
}