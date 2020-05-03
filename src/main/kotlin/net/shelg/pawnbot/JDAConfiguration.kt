package net.shelg.pawnbot

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JDAConfiguration {
    @Bean
    fun jda(@Value("\${discord.bot.token}") botToken: String): JDA {
        val logger = LoggerFactory.getLogger(JDAConfiguration::class.java)
        logger.info("Logging on with token $botToken")
        return JDABuilder
                .createDefault(botToken)
                .setAudioSendFactory(NativeAudioSendFactory())
                .build()
    }
}