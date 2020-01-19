package net.shelg.pawnbot.configuration

import net.dv8tion.jda.api.entities.Guild
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
interface GuildConfigurationValueRepository : CrudRepository<GuildConfigurationProperty, String>

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
    fun getInt(guild: Guild, property: ConfigProperty) =
            property.format.toInt(getString(guild, property))

    fun getString(guild: Guild, property: ConfigProperty) =
            getProperty(guild, property).value

    private fun getProperty(guild: Guild, property: ConfigProperty) =
            repository.findByIdOrNull("${guild.idLong}_${property.key}")
                ?: repository.save(
                        GuildConfigurationProperty(
                                id = "${guild.idLong}_${property.key}",
                                guildId = guild.idLong,
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