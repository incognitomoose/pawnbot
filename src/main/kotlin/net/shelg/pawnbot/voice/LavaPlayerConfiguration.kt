package net.shelg.pawnbot.voice

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LavaPlayerConfiguration {
    @Bean
    fun audioPlayerManager() = DefaultAudioPlayerManager().also(AudioSourceManagers::registerLocalSource)
}