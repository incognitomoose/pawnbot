package net.shelg.pawnbot.events

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.pornhub.PornhubComment
import org.springframework.stereotype.Service

@Service
class EventHub(private val listeners: List<EventListener>) {
    fun fireCommentUsed(comment: PornhubComment, triggerMessage: Message? = null, responseMessage: Message? = null) {
        listeners.forEach { it.onCommentUsed(comment, triggerMessage, responseMessage) }
    }

    fun fireSpeakableTextRelayed(text: String, triggerMessage: Message? = null, responseMessage: Message? = null) {
        listeners.forEach { it.onSpeakableTextRelayed(text, triggerMessage, responseMessage) }
    }
}