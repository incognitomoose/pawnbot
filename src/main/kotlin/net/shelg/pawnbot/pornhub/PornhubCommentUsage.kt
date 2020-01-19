package net.shelg.pawnbot.pornhub

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import javax.persistence.*

@Entity
data class PornhubCommentUsage(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,

        @ManyToOne
        @JoinColumn
        val comment: PornhubComment,

        val timestamp: Instant,

        val channelId: Long,
        val messageId: Long
) {
    override fun toString(): String {
        return "PornhubCommentUsage(id=$id, commentId=${comment.id}, timestamp=$timestamp, channelId=$channelId, messageId=$messageId)"
    }
}


@Repository
interface PornhubCommentUsageRepository : CrudRepository<PornhubCommentUsage, Long> {
    fun findFirstByChannelIdOrderByTimestampDesc(channelId: Long): PornhubCommentUsage?
    fun findByCommentText(commentText: String): PornhubCommentUsage?
    fun findByMessageId(messageId: Long): PornhubCommentUsage?
}