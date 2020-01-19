package net.shelg.pawnbot.triggers.commands.config

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.configuration.*
import net.shelg.pawnbot.formatters.DiscordFormatter.bold
import net.shelg.pawnbot.formatters.DiscordFormatter.italics
import net.shelg.pawnbot.formatters.DiscordFormatter.underline
import net.shelg.pawnbot.triggers.commands.AbstractChatSubcommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import org.springframework.stereotype.Service

@Service
class ConfigServerChatSubcommand(
        private val configService: ConfigService,
        cliParser: CommandLineParser,
        private val textSender: TextSender
) : AbstractChatSubcommand(cliParser, textSender) {

    override fun commandSyntax() = "server [key] [value]"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = respond(args["key"], args["value"], context.guild)
        textSender.sendMessage(response, context.textChannel)
    }

    private fun respond(key: String?, value: String?, guild: Guild): String {
        val allProperties: Sequence<ConfigPropertyDefinition> =
                ServerConfigPropertyDefinition.values().asSequence()
                        .plus(ChannelConfigPropertyDefinition.values().asSequence())
        return if (key == null) {
            underline(bold("Current configuration for ${guild.name}\n")) +
                    allProperties.joinToString(separator = "\n") {
                        getPropertyLine(it, guild)
                    }
        } else {
            val property = allProperties.find { it.key.equals(key, ignoreCase = true) }
            if (property == null) {
                "Invalid key. Please use command without arguments to list keys."
            } else {
                if (value == null) {
                    getPropertyLine(property, guild)
                } else {
                    try {
                        configService.setServerProperty(property, value, guild)
                        "Set ${property.description} to $value for this server"
                    } catch (e: InvalidConfigValueException) {
                        "Invalid value for ${property.key}: ${e.message}"
                    }
                }
            }
        }
    }

    private fun getPropertyLine(property: ConfigPropertyDefinition, guild: Guild) =
            "${bold(property.description)} (${italics(property.key)}): " +
                    configService.getServerValueWithScope(property, guild).let { (value, scope) ->
                        value.plus(
                                scope.takeUnless { it == ConfigValueScope.SERVER }
                                        ?.let { " ${italics("(${scope.description})")}" }
                                        .orEmpty()
                        )
                    }
}