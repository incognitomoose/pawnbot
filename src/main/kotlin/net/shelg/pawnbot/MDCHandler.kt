package net.shelg.pawnbot

import net.dv8tion.jda.api.entities.GuildChannel
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.MDC

object MDCHandler {
    fun clearMDC() {
        MDC.remove("guildName")
        MDC.remove("guildId")
        MDC.remove("channelName")
        MDC.remove("channelId")
        MDC.remove("authorEffectiveName")
        MDC.remove("authorTag")
        MDC.remove("authorId")
    }

    fun setMDC(message: Message) {
        val channel = message.channel
        val guild = if (channel is GuildChannel) channel.guild else null
        MDC.put("guildName", guild?.name ?: channel.type.toString())
        MDC.put("guildId", guild?.id)
        MDC.put("channelName", (if (channel is TextChannel) "#" else "") + channel.name)
        MDC.put("channelId", channel.id)
        MDC.put("authorEffectiveName", message.member?.effectiveName ?: message.author.name)
        MDC.put("authorTag", message.author.asTag)
        MDC.put("authorId", message.author.id)
    }
}