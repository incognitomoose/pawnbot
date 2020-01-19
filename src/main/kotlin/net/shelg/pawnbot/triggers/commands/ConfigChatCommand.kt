package net.shelg.pawnbot.triggers.commands

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.configuration.ConfigProperty
import net.shelg.pawnbot.configuration.GuildConfigurationService
import net.shelg.pawnbot.formatters.DiscordFormatter.bold
import net.shelg.pawnbot.formatters.DiscordFormatter.italics
import net.shelg.pawnbot.formatters.DiscordFormatter.underline
import org.springframework.stereotype.Component

@Component
class ConfigChatCommand(
        cliParser: CommandLineParser,
        private val configService: GuildConfigurationService,
        private val textSender: TextSender
) : AbstractChatCommand(cliParser, textSender, configService) {

    override fun commandSyntax() = "config [key] [value]"
    override fun description() = "Change bot configuration for this server"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = respond(args["key"], args["value"], context.guild)
        textSender.sendMessage(response, context.textChannel)
    }

    private fun respond(key: String?, value: String?, guild: Guild) =
            if (key == null) {
                underline(bold("Current configuration for ${guild.name}\n")) +
                        ConfigProperty.values().joinToString(separator = "\n") {
                            "${bold(it.description)} (${italics(it.key)}): ${configService.getString(guild, it)}"
                        }
            } else {
                val property = ConfigProperty.values().find { it.key.equals(key, ignoreCase = true) }
                if (property == null) {
                    "Invalid key. Please use command without arguments to list config keys."
                } else {
                    if (value == null) {
                        "${bold(property.description)} (${italics(property.key)}): " +
                                configService.getString(guild, property)
                    } else {
                        // Change config
                        if (property.format.isValid(value)) {
                            configService.setProperty(guild, property, value)
                            "Set ${property.description} to $value"
                        } else {
                            "Invalid value for ${property.key}"
                        }
                    }
                }
            }
}