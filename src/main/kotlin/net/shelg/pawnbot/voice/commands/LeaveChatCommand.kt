package net.shelg.pawnbot.voice.commands

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.triggers.commands.AbstractChatCommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import net.shelg.pawnbot.voice.VoiceComponent
import org.springframework.stereotype.Component

@Component
class LeaveChatCommand(
        cliParser: CommandLineParser,
        configService: ConfigService,
        private val textSender: TextSender,
        private val voice: VoiceComponent
) : AbstractChatCommand(cliParser, textSender, configService) {

    override fun commandSyntax() = "leave"
    override fun description() = "Leave voice chat channel"

    override fun execute(args: Map<String, String>, context: Message) {
        val guild = context.guild
        if (voice.isInVoiceChat(guild)) {
            voice.leaveVoiceChat(guild)
        } else {
            textSender.sendMessage("I'm not in a voice chat.", context.textChannel)
        }
    }
}