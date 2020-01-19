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
class MediaTrigger(
        private val configService: GuildConfigurationService,
        private val textSender: TextSender,
        private val commentService: PornhubCommentService,
        private val eventHub: EventHub
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
                    && configuredToReactToMedia(message)
                    && message.textChannel.isNSFW
                    && message.textChannel.canTalkLogIfNot()
                    && message.hasMediaOrLink()

    private fun configuredToReactToMedia(message: Message) =
            configService.getString(message.guild, ConfigProperty.MEDIA_REACTION_CHANNELS)
                    .split(",")
                    .asSequence()
                    .map(String::trim)
                    .any { it == message.textChannel.idLong.toString() }

    override fun handleTrigger(message: Message) {
        val channel = message.textChannel
        textSender.startTyping(channel)
        val guild = message.guild
        val percentGay = configService.getInt(guild, ConfigProperty.PERCENT_GAY)
        val numWordsInterval = configService.getReactionNumWordsInterval(guild)
        val comment = commentService.getRandomComment(percentGay, numWordsInterval.first, numWordsInterval.second)
        if (comment != null) {
            textSender.sendMessage(comment.text, channel, false) {
                eventHub.fireCommentUsed(comment, message, it)
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MediaTrigger::class.java)
    }
}