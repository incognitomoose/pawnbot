package net.shelg.pawnbot.events

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.pornhub.PornhubComment
import org.springframework.stereotype.Service

@Service
class EventHub(private val listeners: List<EventListener>) {
    fun guildChatCommentResponse(triggerMessage: Message, responseMessage: Message, comment: PornhubComment) {
        listeners.forEach { it.guildChatCommentResponse(triggerMessage, responseMessage, comment) }
    }
}