package net.shelg.pawnbot.triggers.commands.config

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.triggers.commands.AbstractChatCommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import org.springframework.stereotype.Component

@Component
class ConfigChatCommand(
        cliParser: CommandLineParser,
        configService: ConfigService,
        private val textSender: TextSender,
        configChannelChatSubcommand: ConfigChannelChatSubcommand,
        configServerChatSubcommand: ConfigServerChatSubcommand
) : AbstractChatCommand(cliParser, textSender, configService) {

    private val subCommands = listOf(
            configChannelChatSubcommand,
            configServerChatSubcommand
    )

    override fun commandSyntax() = "config {scope} [args]"
    override fun description() = "Show or change configuration"

    override fun execute(args: Map<String, String>, context: Message) {
        val commandPrefix = getCommandPrefix(context.textChannel)
        val subCommand = args["scope"]
        val subArgs = args["args"]

        subCommands.find { it.command().equals(subCommand, ignoreCase = true) }
                ?.executeCLI(subArgs, context, "${commandPrefix + command()} ")
                ?: textSender.sendMessage("Invalid scope $subCommand\n" +
                        usage(commandPrefix), context.textChannel)
    }

    override fun usage(commandPrefix: String) =
            "One of:\n" + subCommands.joinToString(separator = "\n") { it.usage(commandPrefix + command() + " ") }
}