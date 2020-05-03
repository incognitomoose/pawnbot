package net.shelg.pawnbot.events

import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.pornhub.PornhubComment
import net.shelg.pawnbot.voice.VoiceComponent
import org.springframework.stereotype.Component

@Component
class VoiceEchoer(private val voice: VoiceComponent) : AbstractEventListener() {
    override fun onSpeakableTextRelayed(text: String, triggerMessage: Message?, responseMessage: Message?) {
        if (triggerMessage != null) {
            voice.speakText(triggerMessage.guild, text)
        }
    }
}