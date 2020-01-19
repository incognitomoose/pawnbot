package net.shelg.pawnbot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

/**
 * LavaPlayer to JDA integration from https://github.com/sedmelluq/LavaPlayer#jda-integration
 */
internal class LavaPlayerAudioSendHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
    private var currentFrame: AudioFrame? = null

    override fun canProvide(): Boolean {
        if (currentFrame == null) {
            currentFrame = audioPlayer.provide()
        }
        return currentFrame != null
    }

    override fun isOpus() = true

    override fun provide20MsAudio() =
            if (canProvide()) {
                val frame = currentFrame!!
                currentFrame = null
                ByteBuffer.wrap(frame.data)
            } else null
}