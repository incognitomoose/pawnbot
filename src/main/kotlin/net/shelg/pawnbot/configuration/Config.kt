package net.shelg.pawnbot.configuration

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.configuration.ChannelConfigPropertyDefinition.*
import net.shelg.pawnbot.configuration.ServerConfigPropertyDefinition.*
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import javax.persistence.Entity
import javax.persistence.Id

interface GuildIdAndChannelId {
    val guildId: Long
    val channelId: Long?
}

@Entity
data class ConfigProperty(
        @Id
        val id: String,

        val guildId: Long,
        val channelId: Long?,
        val key: String,
        val value: String
)

@Repository
interface ConfigPropertyRepository : CrudRepository<ConfigProperty, String> {
    @Query("select distinct p.guildId as guildId, p.channelId as channelId from ConfigProperty p")
    fun findAllDistinctGuildIdsAndChannelIds(): List<GuildIdAndChannelId>
}

class InvalidConfigValueException(message: String) : Exception(message)

private fun validateBoolean(value: String) {
    if (!(value == "true" || value == "false")) throw InvalidConfigValueException("must be \"true\" or \"false\"")
}

private fun validateCommentNumwordsMin(value: String) {
    val intVal = value.toIntOrNull() ?: throw InvalidConfigValueException("must be numeric")
    if (intVal !in 1..8) throw InvalidConfigValueException("must be between 1 and 8")
}

private fun validateCommentNumwordsMax(value: String) {
    val intVal = value.toIntOrNull() ?: throw InvalidConfigValueException("must be numeric")
    if (intVal < 1) throw InvalidConfigValueException("must be at least 1")
}

interface ConfigPropertyDefinition {
    val description: String
    val key: String
    val defaultValue: String

    fun validate(value: String)
    fun isValid(value: String) = runCatching { validate(value) }.isSuccess
}

enum class ServerConfigPropertyDefinition(
        override val description: String,
        override val key: String,
        override val defaultValue: String
) : ConfigPropertyDefinition {
    VOICE_CHAT_INTRODUCTION_COMMENT_ENABLED("Voice chat introduction comment enabled", "voice_chat.introduction_comment.enabled", "true") {
        override fun validate(value: String) = validateBoolean(value)
    },
    VOICE_CHAT_INTRODUCTION_COMMENT_NUMWORDS_MIN("Voice chat introduction comment minimum words", "voice_chat.introduction_comment.numwords.min", "4") {
        override fun validate(value: String) = validateCommentNumwordsMin(value)
    },
    VOICE_CHAT_INTRODUCTION_COMMENT_NUMWORDS_MAX("Voice chat introduction comment maximum words", "voice_chat.introduction_comment.numwords.max", "4") {
        override fun validate(value: String) = validateCommentNumwordsMax(value)
    };
}

enum class ChannelConfigPropertyDefinition(
        override val description: String,
        override val key: String,
        override val defaultValue: String
) : ConfigPropertyDefinition {
    PREFIX("Command prefix", "prefix", "!pb") {
        override fun validate(value: String) = Unit
    },
    GAY_PERCENTAGE("Gay percentage", "gay_percentage", "50") {
        override fun validate(value: String) {
            val intVal = value.toIntOrNull() ?: throw InvalidConfigValueException("must be numeric")
            if (intVal !in 0..100) throw InvalidConfigValueException("must be between 0 and 100")
        }
    },
    REACTIONS_MEDIA_ENABLED("Media reactions enabled", "reactions.media.enabled", "false") {
        override fun validate(value: String) = validateBoolean(value)
    },
    REACTIONS_MEDIA_NUMWORDS_MIN("Media reactions minimum words", "reactions.media.numwords.min", "6") {
        override fun validate(value: String) = validateCommentNumwordsMin(value)
    },
    REACTIONS_MEDIA_NUMWORDS_MAX("Media reactions maximum words", "reactions.media.numwords.max", "256") {
        override fun validate(value: String) = validateCommentNumwordsMax(value)
    },
    REACTIONS_MENTION_ENABLED("Mention reactions enabled", "reactions.mention.enabled", "true") {
        override fun validate(value: String) = validateBoolean(value)
    },
    REACTIONS_MENTION_NUMWORDS_MIN("Mention reactions minimum words", "reactions.mention.numwords.min", "6") {
        override fun validate(value: String) = validateCommentNumwordsMin(value)
    },
    REACTIONS_MENTION_NUMWORDS_MAX("Mention reactions maximum words", "reactions.mention.numwords.max", "256") {
        override fun validate(value: String) = validateCommentNumwordsMax(value)
    };
}

enum class ConfigValueScope(val description: String) {
    HARDCODED("bot default"),
    SERVER("server default"),
    CHANNEL("channel default")
}

@Service
class ConfigService(val repository: ConfigPropertyRepository) {
    fun prefix(channel: TextChannel) = getChannelProperty(PREFIX, channel)

    fun gayPercentage(channel: TextChannel) = getChannelProperty(GAY_PERCENTAGE, channel).toInt()

    fun gayPercentage(guild: Guild) = getServerProperty(GAY_PERCENTAGE, guild).toInt()

    fun reactionsMediaEnabled(channel: TextChannel) =
            getChannelProperty(REACTIONS_MEDIA_ENABLED, channel) == "true"

    fun reactionsMediaNumwordsInterval(channel: TextChannel) =
            getChannelProperty(REACTIONS_MEDIA_NUMWORDS_MIN, channel).toInt() to
                    getChannelProperty(REACTIONS_MEDIA_NUMWORDS_MAX, channel).toInt()

    fun reactionsMentionEnabled(channel: TextChannel) =
            getChannelProperty(REACTIONS_MENTION_ENABLED, channel) == "true"

    fun reactionsMentionNumwordsInterval(channel: TextChannel) =
            getChannelProperty(REACTIONS_MENTION_NUMWORDS_MIN, channel).toInt() to
                    getChannelProperty(REACTIONS_MENTION_NUMWORDS_MAX, channel).toInt()

    fun voiceChatIntroductionCommentEnabled(guild: Guild) =
            getServerProperty(VOICE_CHAT_INTRODUCTION_COMMENT_ENABLED, guild) == "true"

    fun voiceChatIntroductionCommentNumWordsInterval(guild: Guild) =
            getServerProperty(VOICE_CHAT_INTRODUCTION_COMMENT_NUMWORDS_MIN, guild).toInt() to
                    getServerProperty(VOICE_CHAT_INTRODUCTION_COMMENT_NUMWORDS_MAX, guild).toInt()

    private fun idForProperty(property: ConfigPropertyDefinition, guildId: Long, channelId: Long? = null) =
            "${guildId}_${channelId ?: "default"}_${property.key}"

    private fun idForChannelProperty(property: ChannelConfigPropertyDefinition, channel: TextChannel) =
            idForProperty(property, channel.guild.idLong, channel.idLong)

    fun getChannelProperty(property: ChannelConfigPropertyDefinition, channel: TextChannel) =
            getChannelValueWithScope(property, channel).component1()

    fun getChannelValueWithScope(property: ChannelConfigPropertyDefinition, channel: TextChannel) =
            repository.findByIdOrNull(idForChannelProperty(property, channel))?.value?.takeIf(property::isValid)?.let { it to ConfigValueScope.CHANNEL }
                    ?: getServerValueWithScope(property, channel.guild)

    fun setChannelProperty(property: ChannelConfigPropertyDefinition, value: String, channel: TextChannel) {
        property.validate(value)
        repository.save(ConfigProperty(
                id = idForChannelProperty(property, channel),
                guildId = channel.guild.idLong,
                channelId = channel.idLong,
                key = property.key,
                value = value
        ))
    }

    private fun idForServerProperty(property: ConfigPropertyDefinition, guild: Guild) =
            idForProperty(property, guild.idLong)

    fun getServerProperty(property: ConfigPropertyDefinition, guild: Guild) =
            getServerValueWithScope(property, guild).component1()

    fun getServerValueWithScope(property: ConfigPropertyDefinition, guild: Guild) =
            repository.findByIdOrNull(idForServerProperty(property, guild))?.value?.takeIf(property::isValid)?.let { it to ConfigValueScope.SERVER }
                    ?: property.defaultValue to ConfigValueScope.HARDCODED

    fun setServerProperty(property: ConfigPropertyDefinition, value: String, guild: Guild) {
        property.validate(value)
        repository.save(ConfigProperty(
                id = idForServerProperty(property, guild),
                guildId = guild.idLong,
                channelId = null,
                key = property.key,
                value = value
        ))
    }

    fun getAllCommentNumWordsIntervals() =
            getAllConfiguredIntervals(
                    sequenceOf(
                            REACTIONS_MEDIA_NUMWORDS_MIN to REACTIONS_MEDIA_NUMWORDS_MAX,
                            REACTIONS_MENTION_NUMWORDS_MIN to REACTIONS_MENTION_NUMWORDS_MAX,
                            VOICE_CHAT_INTRODUCTION_COMMENT_NUMWORDS_MIN to VOICE_CHAT_INTRODUCTION_COMMENT_NUMWORDS_MAX
                    )
                            .map { it.first as ConfigPropertyDefinition to it.second as ConfigPropertyDefinition }
                            .toList()
            )

    private fun getAllConfiguredIntervals(intervalProperties: List<Pair<ConfigPropertyDefinition, ConfigPropertyDefinition>>) =
            repository.findAllDistinctGuildIdsAndChannelIds()
                    .asSequence()
                    .flatMap { guildIdAndChannelId ->
                        intervalProperties.asSequence().map { (minProp, maxProp) ->
                            getSpecificInterval(guildIdAndChannelId, minProp, maxProp)
                        }
                    }
                    .plus(intervalProperties.asSequence().map {
                        it.first.defaultValue.toInt() to it.second.defaultValue.toInt()
                    })
                    .distinct()
                    .toList()

    private fun getSpecificInterval(
            guildIdAndChannelId: GuildIdAndChannelId,
            minProp: ConfigPropertyDefinition,
            maxProp: ConfigPropertyDefinition
    ) = getSpecificValue(guildIdAndChannelId.guildId, guildIdAndChannelId.channelId, minProp).toInt() to
            getSpecificValue(guildIdAndChannelId.guildId, guildIdAndChannelId.channelId, maxProp).toInt()

    private fun getSpecificValue(guildId: Long, channelId: Long?, property: ConfigPropertyDefinition) =
            repository.findByIdOrNull(idForProperty(property, guildId, channelId))?.value?.takeIf(property::isValid)
                    ?: property.defaultValue
}