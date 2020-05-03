package net.shelg.pawnbot.triggers.reactions

import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.censoring.Censor
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.events.EventHub
import net.shelg.pawnbot.pornhub.PornhubCommentService
import net.shelg.pawnbot.triggers.MessageTrigger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MediaTrigger(
        private val configService: ConfigService,
        private val textSender: TextSender,
        private val commentService: PornhubCommentService,
        private val eventHub: EventHub,
        private val censor: Censor
) : MessageTrigger {

    private fun Message.hasMediaOrLink() =
            when {
                attachments.isNotEmpty() -> {
                    LOGGER.debug("Found " + attachments.size + " attachments in message")
                    true
                }
                embeds.isNotEmpty() -> {
                    LOGGER.debug("Found " + embeds.size + " embeds in message")
                    true
                }
                contentDisplay.contains("https://") -> {
                    LOGGER.debug("Found what looks like a link in message")
                    true
                }
                else -> false
            }

    private fun TextChannel.canTalkLogIfNot() =
            canTalk().also { if (!it) LOGGER.warn("Can't talk in channel $name!") }

    override fun triggersByMessage(message: Message) =
            message.isFromType(ChannelType.TEXT)
                    && configService.reactionsMediaEnabled(message.textChannel)
                    && message.textChannel.canTalkLogIfNot()
                    && message.hasMediaOrLink()

    override fun handleTrigger(message: Message) {
        val channel = message.textChannel
        textSender.startTyping(channel)
        val percentGay = configService.gayPercentage(channel)
        val numWordsInterval = configService.reactionsMediaNumwordsInterval(channel)
        val comment = commentService.getRandomComment(percentGay, numWordsInterval.first, numWordsInterval.second)
        if (comment != null) {
            val censoredtext = censor.censorText(comment.text, channel)
            textSender.sendMessage(censoredtext, channel) {
                eventHub.fireCommentUsed(comment, message, it)
                eventHub.fireSpeakableTextRelayed(censoredtext, message, it)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MediaTrigger::class.java)
    }
}