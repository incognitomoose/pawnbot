package net.shelg.pawnbot.events

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.pornhub.PornhubComment

interface EventListener {
    fun guildChatCommentResponse(triggerMessage: Message, responseMessage: Message, comment: PornhubComment)
}

abstract class AbstractEventListener : EventListener {
    override fun guildChatCommentResponse(triggerMessage: Message, responseMessage: Message, comment: PornhubComment) {}
}