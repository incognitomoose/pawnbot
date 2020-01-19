package net.shelg.pawnbot.triggers.commands.config

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.configuration.ChannelConfigPropertyDefinition
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.configuration.ConfigValueScope
import net.shelg.pawnbot.configuration.InvalidConfigValueException
import net.shelg.pawnbot.formatters.DiscordFormatter.bold
import net.shelg.pawnbot.formatters.DiscordFormatter.italics
import net.shelg.pawnbot.formatters.DiscordFormatter.underline
import net.shelg.pawnbot.triggers.commands.AbstractChatSubcommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import org.springframework.stereotype.Service

@Service
class ConfigChannelChatSubcommand(
        private val configService: ConfigService,
        cliParser: CommandLineParser,
        private val textSender: TextSender
) : AbstractChatSubcommand(cliParser, textSender) {

    override fun commandSyntax() = "channel [key] [value]"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = respond(args["key"], args["value"], context.textChannel)
        textSender.sendMessage(response, context.textChannel)
    }

    private fun respond(key: String?, value: String?, channel: TextChannel) =
            if (key == null) {
                underline(bold("Current configuration for #${channel.name}\n")) +
                        ChannelConfigPropertyDefinition.values().joinToString(separator = "\n") {
                            getPropertyLine(it, channel)
                        }
            } else {
                val property = ChannelConfigPropertyDefinition.values().find { it.key.equals(key, ignoreCase = true) }
                if (property == null) {
                    "Invalid key. Please use command without arguments to list keys."
                } else {
                    if (value == null) {
                        getPropertyLine(property, channel)
                    } else {
                        try {
                            configService.setChannelProperty(property, value, channel)
                            "Set ${property.description} to $value for this channel"
                        } catch (e: InvalidConfigValueException) {
                            "Invalid value for ${property.key}: ${e.message}"
                        }
                    }
                }
            }

    private fun getPropertyLine(property: ChannelConfigPropertyDefinition, channel: TextChannel) =
            "${bold(property.description)} (${italics(property.key)}): " +
                    configService.getChannelValueWithScope(property, channel).let { (value, scope) ->
                        value.plus(
                                scope.takeUnless { it == ConfigValueScope.CHANNEL }
                                        ?.let { " ${italics("(${scope.description})")}" }
                                        .orEmpty()
                        )
                    }
}