package net.shelg.pawnbot.configuration

import net.dv8tion.jda.api.entities.Guild
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class GuildConfigurationProperty(
        @Id
        val id: String,

        val guildId: Long,
        val key: String,
        val value: String
)

@Repository
interface GuildConfigurationValueRepository : CrudRepository<GuildConfigurationProperty, String> {
    @Query("select distinct p.guildId from GuildConfigurationProperty p")
    fun findAllDistinctGuildIds(): List<Long>
}

enum class ConfigValueFormat {
    TEXT {
        override fun isValid(value: String) = true
    },
    NUMERIC {
        override fun isValid(value: String) = Regex("^(-)?[0-9]+$").matches(value)
        override fun toInt(value: String) = value.toInt()
    };

    abstract fun isValid(value: String): Boolean

    open fun toInt(value: String): Int =
            throw IllegalArgumentException(this.name + " cannot be converted to Int")
}

enum class ConfigProperty(
        val description: String,
        val key: String,
        val format: ConfigValueFormat,
        val defaultValue: String
) {
    COMMAND_PREFIX("Command prefix", "command.prefix", ConfigValueFormat.TEXT, "!pb"),
    PERCENT_GAY("Percent gay", "percent.gay", ConfigValueFormat.NUMERIC, "10"),
    REACTION_NUM_WORDS_MIN("Minimum number of words in reaction", "reaction.num.words.min", ConfigValueFormat.NUMERIC, "6"),
    REACTION_NUM_WORDS_MAX("Maximum number of words in reaction", "reaction.num.words.max", ConfigValueFormat.NUMERIC, "256"),
    MEDIA_REACTION_CHANNELS("Channels in which to react to media", "media.reaction.channels", ConfigValueFormat.TEXT, ""),
    CENSOR_TEXT_CHANNELS("Channels in which to censor text", "censor.text.channels", ConfigValueFormat.TEXT, ""),
}

@Service
class GuildConfigurationService(val repository: GuildConfigurationValueRepository) {
    fun getAllCommentNumWordsIntervals() =
            repository.findAllDistinctGuildIds()
                    .asSequence()
                    .flatMap {
                        sequenceOf(
                                getReactionNumWordsInterval(it),
                                getVoiceChatJoinNumWordsInterval(it)
                        )
                    }
                    .plus(
                            sequenceOf(
                                    ConfigProperty.REACTION_NUM_WORDS_MIN.defaultValue.toInt() to
                                            ConfigProperty.REACTION_NUM_WORDS_MAX.defaultValue.toInt(),
                                    4 to 4
                            )
                    )
                    .distinct()
                    .toList()

    fun getReactionNumWordsInterval(guild: Guild) = getReactionNumWordsInterval(guild.idLong)

    fun getVoiceChatJoinNumWordsInterval(guild: Guild) = getVoiceChatJoinNumWordsInterval(guild.idLong)

    fun getVoiceChatJoinNumWordsInterval(guildId: Long) = 4 to 4 // Per now implicit

    private fun getReactionNumWordsInterval(guildId: Long) =
            getInt(guildId, ConfigProperty.REACTION_NUM_WORDS_MIN) to
                    getInt(guildId, ConfigProperty.REACTION_NUM_WORDS_MAX)

    fun getInt(guild: Guild, property: ConfigProperty) = getInt(guild.idLong, property)

    private fun getInt(guildId: Long, property: ConfigProperty) =
            property.format.toInt(getString(guildId, property))

    fun getString(guild: Guild, property: ConfigProperty) = getString(guild.idLong, property)

    private fun getString(guildId: Long, property: ConfigProperty) = getProperty(guildId, property).value

    private fun getProperty(guildId: Long, property: ConfigProperty) =
            repository.findByIdOrNull("${guildId}_${property.key}")
                    ?: repository.save(
                            GuildConfigurationProperty(
                                    id = "${guildId}_${property.key}",
                                    guildId = guildId,
                                    key = property.key,
                                    value = property.defaultValue
                            )
                    )

    fun setProperty(guild: Guild, property: ConfigProperty, value: String) {
        repository.save(
                GuildConfigurationProperty(
                        id = "${guild.idLong}_${property.key}",
                        guildId = guild.idLong,
                        key = property.key,
                        value = value
                )
        )
    }
}