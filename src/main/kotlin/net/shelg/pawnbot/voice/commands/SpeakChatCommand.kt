package net.shelg.pawnbot.voice.commands

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.censoring.Censor
import net.shelg.pawnbot.triggers.commands.AbstractChatCommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import net.shelg.pawnbot.voice.VoiceComponent
import org.springframework.stereotype.Component

@Component
class SpeakChatCommand(
        cliParser: CommandLineParser,
        configService: ConfigService,
        private val textSender: TextSender,
        private val voice: VoiceComponent,
        private val censor: Censor
) : AbstractChatCommand(cliParser, textSender, configService) {

    override fun commandSyntax() = "speak [text]"
    override fun description() = "Speak text in voice chat"

    override fun execute(args: Map<String, String>, context: Message) {
        val channel = context.textChannel
        val guild = channel.guild
        if (voice.isInVoiceChat(guild)) {
            val text = args["text"] ?: "Woof"
            val censoredText = censor.censorText(text, channel)
            voice.speakText(guild, censoredText)
        } else {
            textSender.sendMessage("I can't speak when I'm not in a voice chat.", channel)
        }
    }
}