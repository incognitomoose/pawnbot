package net.shelg.pawnbot.events

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.pornhub.PornhubComment

interface EventListener {
    fun onCommentUsed(comment: PornhubComment, triggerMessage: Message?, responseMessage: Message?)
}

abstract class AbstractEventListener : EventListener {
    override fun onCommentUsed(comment: PornhubComment, triggerMessage: Message?, responseMessage: Message?) {}
}