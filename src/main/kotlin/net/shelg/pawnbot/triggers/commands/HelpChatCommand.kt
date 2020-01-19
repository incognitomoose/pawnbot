package net.shelg.pawnbot.triggers.commands

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.configuration.GuildConfigurationService
import net.shelg.pawnbot.formatters.DiscordFormatter.bold
import org.springframework.stereotype.Component

@Component
class HelpChatCommand(
        cliParser: CommandLineParser,
        configService: GuildConfigurationService,
        private val textSender: TextSender,
        private val chatCommands: List<AbstractChatCommand>
) : AbstractChatCommand(cliParser, textSender, configService) {

    override fun commandSyntax() = "help"
    override fun description() = "Show which commands are available"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = respond(context.guild)
        textSender.sendMessage(response, context.textChannel)
    }

    private fun respond(guild: Guild) =
            "${bold("The following commands are available:")}\n" +
                    chatCommands.plus(this)
                            .sortedBy(AbstractChatCommand::command)
                            .joinToString(separator = "\n") {
                                bold(it.commandWithPrefix(guild)) + " - " + it.description()
                            }
}