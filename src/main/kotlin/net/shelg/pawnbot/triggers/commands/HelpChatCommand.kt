package net.shelg.pawnbot.triggers.commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.formatters.DiscordFormatter.bold
import org.springframework.stereotype.Component

@Component
class HelpChatCommand(
        cliParser: CommandLineParser,
        configService: ConfigService,
        private val textSender: TextSender,
        private val chatCommands: List<AbstractChatCommand>
) : AbstractChatCommand(cliParser, textSender, configService) {

    override fun commandSyntax() = "help"
    override fun description() = "Show which commands are available"

    override fun execute(args: Map<String, String>, context: Message) {
        val channel = context.textChannel
        textSender.startTyping(channel)
        val response = respond(channel)
        textSender.sendMessage(response, channel)
    }

    private fun respond(channel: TextChannel) =
            "${bold("The following commands are available:")}\n" +
                    chatCommands.plus(this)
                            .sortedBy(AbstractChatCommand::command)
                            .joinToString(separator = "\n") {
                                bold(it.commandWithPrefix(channel)) + " - " + it.description()
                            }
}