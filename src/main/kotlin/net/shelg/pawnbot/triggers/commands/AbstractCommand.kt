package net.shelg.pawnbot.triggers.commands

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.formatters.DiscordFormatter

abstract class AbstractCommand(
        private val cliParser: CommandLineParser,
        private val textSender: TextSender
) {
    protected abstract fun commandSyntax(): String
    protected abstract fun execute(args: Map<String, String>, context: Message)

    fun command() = commandSyntax().substringBefore(" ")

    private fun usageWithHeader(prefix: String) =
            "${DiscordFormatter.bold("Usage:")}\n" + usage(prefix)

    open fun usage(commandPrefix: String) =
            "${commandPrefix}${commandSyntax()
                    .replace('{', '<')
                    .replace('}', '>')
            }"

    fun executeCLI(cliArgLine: String?, context: Message, prefix: String) {
        val result = runCatching {
            cliParser.parseArgs(cliArgLine, commandSyntax().split(' ', limit = 2).getOrNull(1))
        }
        if (result.isSuccess) {
            execute(result.getOrThrow(), context)
        } else {
            val exception = result.exceptionOrNull()
            if (exception is CommandLineParser.RequiredArgMissing) {
                textSender.sendMessage(
                        "Missing ${exception.argName} argument.\n" +
                                usageWithHeader(prefix), context.textChannel)
            } else {
                textSender.sendMessage(
                        "Invalid command syntax.\n" +
                                usageWithHeader(prefix), context.textChannel)
            }
        }
    }
}