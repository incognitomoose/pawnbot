package net.shelg.pawnbot.triggers.commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.censoring.Censor
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.events.EventHub
import net.shelg.pawnbot.formatters.DiscordFormatter.bold
import net.shelg.pawnbot.formatters.DiscordFormatter.quote
import net.shelg.pawnbot.pornhub.PornhubComment
import net.shelg.pawnbot.pornhub.PornhubCommentUsageRepository
import org.springframework.stereotype.Component

@Component
class SauceChatCommand(
        cliParser: CommandLineParser,
        configService: ConfigService,
        private val textSender: TextSender,
        private val commentUsageRepo: PornhubCommentUsageRepository,
        private val eventHub: EventHub,
        private val censor: Censor
) : AbstractChatCommand(cliParser, textSender, configService) {

    override fun commandSyntax() = "sauce [comment_or_message_id]"
    override fun description() = "Show the source of a posted comment"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = respond(args["comment_or_message_id"], context.textChannel)
        textSender.sendMessage(response, context.textChannel)
    }

    private fun respond(commentOrMessageId: String?, channel: TextChannel) =
            getUsageOrError(commentOrMessageId, channel)
                    .let { (commentUsage, error) ->
                        commentUsage?.comment
                                ?.also { comment ->
                                    comment.otherVideoComments().forEach { eventHub.fireCommentUsed(it) }
                                }
                                ?.let { formatUsageSource(it, channel) }
                                ?: error!!
                    }

    private fun getUsageOrError(commentOrMessageId: String?, channel: TextChannel) =
            when {
                commentOrMessageId == null -> {
                    commentUsageRepo.findFirstByChannelIdOrderByTimestampDesc(channel.idLong)
                            ?.let { it to null }
                            ?: null to "I don't believe I've posted any comment here."
                }
                commentOrMessageId.matches(REGEX_NUMERIC) -> {
                    commentUsageRepo.findByMessageId(commentOrMessageId.toLong())
                            ?.let { it to null }
                            ?: null to "I don't remember writing a message with that message ID."
                }
                else -> {
                    commentUsageRepo.findByCommentText(commentOrMessageId)
                            ?.let { it to null }
                            ?: null to "I don't remember writing a message with that content."
                }
            }

    private fun formatUsageSource(comment: PornhubComment, channel: TextChannel): String =
            censor.censorText(quote(comment.text), channel) +
                    "was posted by user ${censor.censorText(comment.user, channel)} on ${if (comment.video.gay) "gay" else "straight"} video\n" +
                    "${bold(censor.censorText(comment.video.title, channel))}\n" +
                    "(${comment.video.url.replace("http", "hxxp", ignoreCase = true)})\n" +
                    if (comment.video.comments.size > 1) {
                        "The other comments on the video were:\n" +
                                comment.otherVideoComments()
                                        .joinToString(prefix = "> ", separator = "\n> ", postfix = "\n") {
                                            censor.censorText(it.text, channel)
                                        }
                    } else {
                        "It was the only comment on that video."
                    }

    companion object {
        private val REGEX_NUMERIC = Regex("^(-)?[0-9]+$")
    }
}