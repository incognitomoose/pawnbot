package net.shelg.pawnbot.voice

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import org.slf4j.LoggerFactory
import java.io.File

class AudioPlayerQueueManager(private val player: AudioPlayer) : AudioEventAdapter(), AudioLoadResultHandler {
    private var playing = false
    private val queue = mutableListOf<AudioTrack>()

    override fun loadFailed(throwable: FriendlyException) = LOGGER.error("Load of track failed!", throwable)
    override fun noMatches() = LOGGER.error("No matches when loading track!")
    override fun onPlayerPause(player: AudioPlayer) = LOGGER.info("Audio player paused")
    override fun onPlayerResume(player: AudioPlayer) = LOGGER.info("Audio player resumed")

    override fun onTrackException(player: AudioPlayer, track: AudioTrack, exception: FriendlyException) =
            LOGGER.error("Exception when playing track " + track.identifier + "!", exception)

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        LOGGER.info("Track ended, deleting file " + track.identifier)
        File(track.identifier).delete()
        playNext()
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        LOGGER.info("Track " + track.identifier + " started")
        playing = true
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) =
            LOGGER.info("Track " + track.identifier + " is stuck!")

    override fun playlistLoaded(playlist: AudioPlaylist) {
        playlist.tracks.forEach { track: AudioTrack -> trackLoaded(track) }
    }

    private fun playNext() {
        if (queue.isEmpty()) {
            playing = false
        } else {
            player.playTrack(queue.removeAt(0))
        }
    }

    override fun trackLoaded(track: AudioTrack) {
        queue.add(track)
        if (!playing) {
            playNext()
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AudioPlayerQueueManager::class.java)
    }

    init {
        player.addListener(this)
    }
}