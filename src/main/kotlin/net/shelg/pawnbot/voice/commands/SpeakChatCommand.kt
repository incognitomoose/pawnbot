package net.shelg.pawnbot.voice.commands

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.triggers.commands.AbstractChatCommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import net.shelg.pawnbot.voice.VoiceComponent
import org.springframework.stereotype.Component

@Component
class SpeakChatCommand(
        cliParser: CommandLineParser,
        configService: ConfigService,
        private val textSender: TextSender,
        private val voice: VoiceComponent
) : AbstractChatCommand(cliParser, textSender, configService) {

    override fun commandSyntax() = "speak [text]"
    override fun description() = "Speak text in voice chat"

    override fun execute(args: Map<String, String>, context: Message) {
        val guild = context.guild
        if (voice.isInVoiceChat(guild)) {
            voice.speakText(guild, args["text"] ?: "Woof")
        } else {
            textSender.sendMessage("I can't speak when I'm not in a voice chat.", context.textChannel)
        }
    }
}