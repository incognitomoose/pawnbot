package net.shelg.pawnbot.triggers.commands

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.configuration.ConfigProperty
import net.shelg.pawnbot.configuration.GuildConfigurationService
import net.shelg.pawnbot.triggers.MessageTrigger

abstract class AbstractChatCommand(
        cliParser: CommandLineParser,
        textSender: TextSender,
        private val configService: GuildConfigurationService
) : MessageTrigger, AbstractCommand(cliParser, textSender) {
    abstract fun description(): String

    override fun handleTrigger(message: Message) {
        val cliArgLine = message.contentDisplay.split(' ', limit = 2).getOrNull(1)
        executeCLI(cliArgLine, message, getCommandPrefix(message.guild))
    }

    override fun triggersByMessage(message: Message): Boolean {
        val cliCommand = message.contentDisplay.split(' ', limit = 2)[0]
        return commandWithPrefix(message.guild).equals(cliCommand, ignoreCase = true)
    }

    protected fun getCommandPrefix(guild: Guild) =
            configService.getString(guild, ConfigProperty.COMMAND_PREFIX)

    fun commandWithPrefix(guild: Guild) = getCommandPrefix(guild) + command()
}