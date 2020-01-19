package net.shelg.pawnbot

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.shelg.pawnbot.MDCHandler.clearMDC
import net.shelg.pawnbot.MDCHandler.setMDC
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.pornhub.PornhubCommentService
import net.shelg.pawnbot.triggers.MessageTrigger
import net.shelg.pawnbot.voice.VoiceComponent
import org.slf4j.LoggerFactory
import org.slf4j.MarkerFactory
import org.springframework.stereotype.Service

@Service
class Pawnbot(
        jda: JDA,
        private val configService: ConfigService,
        private val commentService: PornhubCommentService,
        private val voice: VoiceComponent,
        private val messageTriggers: List<MessageTrigger>
) : ListenerAdapter() {

    init {
        @Suppress("LeakingThis")
        jda.addEventListener(this)
    }

    private fun logReceivedMessage(message: Message) {
        LOGGER.info(CHAT_LOG_MARKER, message.contentDisplay)
    }

    override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
        if (event.guild.selfMember == event.member) {
            val percentGay = configService.gayPercentage(event.guild)
            if (configService.voiceChatIntroductionCommentEnabled(event.guild)) {

            }
            val numWordsInterval = configService.voiceChatIntroductionCommentNumWordsInterval(event.guild)
            val comment = commentService.getRandomComment(percentGay, numWordsInterval.first, numWordsInterval.second)
            val text = comment?.text ?: "Hi guys! It's me, " + event.guild.selfMember.effectiveName + "!"
            LOGGER.info("Greeting on VC join with text \"$text\"")
            voice.speakText(event.guild, text)
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message = event.message
        setMDC(message)
        try {
            if (!message.author.isBot && !message.author.isFake && message.isFromGuild) {
                messageTriggers.stream()
                        .filter { it.triggersByMessage(message) }
                        .findFirst()
                        .ifPresent {
                            logReceivedMessage(message)
                            LOGGER.info("Reaction triggered by " + it.javaClass.simpleName)
                            it.handleTrigger(message)
                        }
            }
        } finally {
            clearMDC()
        }
    }

    companion object {
        private val CHAT_LOG_MARKER = MarkerFactory.getMarker("DISCORD_CHAT")
        private val LOGGER = LoggerFactory.getLogger(Pawnbot::class.java)
    }
}