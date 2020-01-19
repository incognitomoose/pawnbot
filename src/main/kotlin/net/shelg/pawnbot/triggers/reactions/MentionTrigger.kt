package net.shelg.pawnbot.triggers.reactions

import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.configuration.ConfigProperty
import net.shelg.pawnbot.configuration.GuildConfigurationService
import net.shelg.pawnbot.events.EventHub
import net.shelg.pawnbot.pornhub.PornhubCommentService
import net.shelg.pawnbot.triggers.MessageTrigger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class MentionTrigger(
        private val configService: GuildConfigurationService,
        private val textSender: TextSender,
        private val commentService: PornhubCommentService,
        private val eventHub: EventHub
) : MessageTrigger {

    private fun TextChannel.canTalkLogIfNot() =
            canTalk().also { if (!it) LOGGER.warn("Can't talk in channel $name!") }

    override fun triggersByMessage(message: Message) =
            message.isFromType(ChannelType.TEXT)
                    && message.textChannel.canTalkLogIfNot()
                    && message.isMentioned(message.jda.selfUser)

    override fun handleTrigger(message: Message) {
        val channel = message.textChannel
        textSender.startTyping(channel)
        val guild = message.guild
        val percentGay = configService.getInt(guild, ConfigProperty.PERCENT_GAY)
        val numWordsInterval = configService.getReactionNumWordsInterval(guild)
        val comment = commentService.getRandomComment(percentGay, numWordsInterval.first, numWordsInterval.second)
        if (comment != null) {
            textSender.sendMessage("${message.author.asMention}: ${comment.text}", channel, false) {
                eventHub.fireCommentUsed(comment, message, it)
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