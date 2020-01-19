package net.shelg.pawnbot.triggers.commands

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.voice.VoiceComponent
import org.springframework.stereotype.Component

@Component
class JoinChatCommand(
        cliParser: CommandLineParser,
        configService: ConfigService,
        private val textSender: TextSender,
        private val voice: VoiceComponent
) : AbstractChatCommand(cliParser, textSender, configService) {

    override fun commandSyntax() = "join"
    override fun description() = "Join voice chat channel"

    override fun execute(args: Map<String, String>, context: Message) {
        val member = context.member!!
        val voiceChannel = member.voiceState?.channel
        if (voiceChannel == null) {
            textSender.sendMessage(member.asMention + ", you need to be in a voice channel for me to join.", context.textChannel)
        } else {
            voice.joinChannel(voiceChannel)
        }
    }
}