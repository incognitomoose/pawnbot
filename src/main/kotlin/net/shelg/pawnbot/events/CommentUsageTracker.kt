package net.shelg.pawnbot.events

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.pornhub.PornhubComment
import net.shelg.pawnbot.pornhub.PornhubCommentRepository
import net.shelg.pawnbot.pornhub.PornhubCommentUsage
import net.shelg.pawnbot.pornhub.PornhubCommentUsageRepository
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CommentUsageTracker(
        private val commentRepo: PornhubCommentRepository,
        private val commentUsageRepo: PornhubCommentUsageRepository
) : AbstractEventListener() {

    override fun onCommentUsed(comment: PornhubComment, triggerMessage: Message?, responseMessage: Message?) {
        if (responseMessage != null) {
            commentUsageRepo.save(PornhubCommentUsage(
                    comment = comment,
                    timestamp = Instant.now(),
                    channelId = responseMessage.channel.idLong,
                    messageId = responseMessage.idLong
            ))
        }

        comment.numUsages++
        commentRepo.save(comment)
    }
}