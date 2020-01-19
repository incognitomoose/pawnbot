package net.shelg.pawnbot.triggers

import net.dv8tion.jda.api.entities.Message

interface MessageTrigger {
    fun handleTrigger(message: Message)
    fun triggersByMessage(message: Message): Boolean
}
