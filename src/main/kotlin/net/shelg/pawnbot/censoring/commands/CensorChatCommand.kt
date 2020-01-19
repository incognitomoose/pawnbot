package net.shelg.pawnbot.censoring.commands

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.triggers.commands.AbstractChatCommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import org.springframework.stereotype.Component

@Component
class CensorChatCommand(
        cliParser: CommandLineParser,
        configService: ConfigService,
        private val textSender: TextSender,
        censorAddSubCommand: CensorAddChatSubcommand,
        censorListSubCommand: CensorListChatSubcommand,
        censorRemoveSubCommand: CensorRemoveChatSubcommand,
        censorTestSubCommand: CensorTestChatSubcommand
) : AbstractChatCommand(cliParser, textSender, configService) {

    private val subCommands = listOf(
            censorAddSubCommand,
            censorListSubCommand,
            censorRemoveSubCommand,
            censorTestSubCommand
    )

    override fun commandSyntax() = "censor {subcommand} [args]"
    override fun description() = "Manage phrases that are censored"

    override fun execute(args: Map<String, String>, context: Message) {
        val commandPrefix = getCommandPrefix(context.textChannel)
        val subCommand = args["subcommand"]
        val subArgs = args["args"]

        subCommands.find { it.command().equals(subCommand, ignoreCase = true) }
                ?.executeCLI(subArgs, context, "${commandPrefix + command()} ")
                ?: textSender.sendMessage("Invalid subcommand $subCommand\n" +
                        usage(commandPrefix), context.textChannel)
    }

    override fun usage(commandPrefix: String) =
            "One of:\n" + subCommands.joinToString(separator = "\n") { it.usage(commandPrefix + command() + " ") }
}