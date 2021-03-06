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
class MentionTrigger(
        private val configService: ConfigService,
        private val textSender: TextSender,
        private val commentService: PornhubCommentService,
        private val eventHub: EventHub,
        private val censor: Censor
) : MessageTrigger {

    private fun TextChannel.canTalkLogIfNot() =
            canTalk().also { if (!it) LOGGER.warn("Can't talk in channel $name!") }

    override fun triggersByMessage(message: Message) =
            message.isFromType(ChannelType.TEXT)
                    && configService.reactionsMentionEnabled(message.textChannel)
                    && message.textChannel.canTalkLogIfNot()
                    && message.isMentioned(message.jda.selfUser)

    override fun handleTrigger(message: Message) {
        val channel = message.textChannel
        textSender.startTyping(channel)
        val percentGay = configService.gayPercentage(channel)
        val numWordsInterval = configService.reactionsMentionNumwordsInterval(channel)
        val comment = commentService.getRandomComment(percentGay, numWordsInterval.first, numWordsInterval.second)
        if (comment != null) {
            val censoredtext = censor.censorText(comment.text, channel)
            textSender.sendMessage("${message.author.asMention}: $censoredtext", channel) {
                eventHub.fireCommentUsed(comment, message, it)
                eventHub.fireSpeakableTextRelayed(censoredtext, message, it)
            }
        } else {
            textSender.sendMessage("${message.author.asMention}: Sorry, I looked through a bunch of random videos," +
                    " but none of them had any usable comments :(", channel)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MentionTrigger::class.java)
    }
}