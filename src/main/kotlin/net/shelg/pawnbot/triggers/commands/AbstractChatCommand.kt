package net.shelg.pawnbot.triggers.commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.triggers.MessageTrigger

abstract class AbstractChatCommand(
        cliParser: CommandLineParser,
        textSender: TextSender,
        private val configService: ConfigService
) : MessageTrigger, AbstractCommand(cliParser, textSender) {
    abstract fun description(): String

    override fun handleTrigger(message: Message) {
        val cliArgLine = message.contentDisplay.split(' ', limit = 2).getOrNull(1)
        executeCLI(cliArgLine, message, getCommandPrefix(message.textChannel))
    }

    override fun triggersByMessage(message: Message): Boolean {
        val cliCommand = message.contentDisplay.split(' ', limit = 2)[0]
        return commandWithPrefix(message.textChannel).equals(cliCommand, ignoreCase = true)
    }

    protected fun getCommandPrefix(channel: TextChannel) = configService.prefix(channel)

    fun commandWithPrefix(channel: TextChannel) = getCommandPrefix(channel) + command()
}