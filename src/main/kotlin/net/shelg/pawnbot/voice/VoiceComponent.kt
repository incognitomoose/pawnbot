package net.shelg.pawnbot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.VoiceChannel
import net.shelg.pawnbot.HardcodedFilter
import net.shelg.pawnbot.censoring.Censor
import net.shelg.pawnbot.voice.tts.GoogleTTSClient
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.util.*

@Service
class VoiceComponent(
        private val audioPlayerManager: AudioPlayerManager,
        private val tts: GoogleTTSClient
) {
    private val filter = HardcodedFilter { filteredWord ->
        when {
            filteredWord.endsWith("s") -> "Twitch Admins"
            else -> "Twitch Admin"
        }
    }

    private val queueManagers: MutableMap<Long, AudioPlayerQueueManager> = HashMap()

    private fun buildTempFilename(text: String?): String {
        val builder = StringBuilder()
        builder.append(System.currentTimeMillis())
        builder.append("_")
        builder.append(StringUtils.left(text!!.replace("[^a-zA-Z]".toRegex(), "_"), 50))
        return builder.toString()
    }

    fun isInVoiceChat(guild: Guild): Boolean {
        return queueManagers[guild.idLong] != null
    }

    fun joinChannel(voiceChannel: VoiceChannel) { // Get guild and audio manager
        val guild = voiceChannel.guild
        val audioManager = guild.audioManager
        // Create audio player and queue manager for voice channel
        val audioPlayer = audioPlayerManager.createPlayer()
        audioManager.sendingHandler = LavaPlayerAudioSendHandler(audioPlayer)
        queueManagers[guild.idLong] = AudioPlayerQueueManager(audioPlayer)
        // Join the channel
        audioManager.openAudioConnection(voiceChannel)
    }

    fun leaveVoiceChat(guild: Guild) {
        guild.audioManager.closeAudioConnection()
        queueManagers.remove(guild.idLong)
    }

    fun speakText(guild: Guild, text: String) {
        val filteredText = filter.apply(text)

        val trackScheduler = queueManagers[guild.idLong]
        if (trackScheduler != null) {
            val file = File(buildTempFilename(filteredText) + ".ogg")
            try {
                tts.textToSpeech(filteredText, file)
            } catch (e: IOException) {
                throw IllegalStateException(e)
            }
            audioPlayerManager.loadItem(file.absolutePath, trackScheduler)
        }
    }
}