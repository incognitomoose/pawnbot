package net.shelg.pawnbot.events

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.pornhub.PornhubComment
import net.shelg.pawnbot.voice.VoiceComponent
import org.springframework.stereotype.Component

@Component
class VoiceReactor(private val voice: VoiceComponent) : AbstractEventListener() {
    override fun onCommentUsed(comment: PornhubComment, triggerMessage: Message?, responseMessage: Message?) {
        if (triggerMessage != null) {
            voice.speakText(triggerMessage.guild, comment.text)
        }
    }
}