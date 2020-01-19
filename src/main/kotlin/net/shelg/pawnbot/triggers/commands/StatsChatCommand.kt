package net.shelg.pawnbot.triggers.commands

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.Uptime
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.formatters.DiscordFormatter.bold
import net.shelg.pawnbot.formatters.DurationFormatter
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component

@Component
class StatsChatCommand(
        cliParser: CommandLineParser,
        configService: ConfigService,
        private val textSender: TextSender,
        private val uptime: Uptime,
        private val buildProperties: BuildProperties
) : AbstractChatCommand(cliParser, textSender, configService) {

    override fun commandSyntax() = "stats"
    override fun description() = "Show statistics for bot"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = respond()
        textSender.sendMessage(response, context.textChannel)
    }

    private fun respond() =
            "${bold("${buildProperties.artifact} version ${buildProperties.version}")}\n" +
                    "${bold("Built at:")} ${buildProperties.time}\n" +
                    "${bold("Uptime:")} ${DurationFormatter.formatDuration(uptime.get())}"
}