package net.shelg.pawnbot.censoring

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class CensorshipReplacementService(private val censorRepo: CensorshipReplacementRepository) {
    fun getReplacementsInGroup(guild: Guild, group: String) =
            getAllReplacementsForGuild(guild).filter { it.groupKey == group }

    fun getAllGroups(guild: Guild) =
            getAllReplacementsForGuild(guild).asSequence().map(CensorshipReplacement::groupKey).distinct().sorted().toList()

    fun getRegexReplacements(guild: Guild, channel: TextChannel?, hardcodedOnly: Boolean) =
            getReplacementsForGuildAndChannel(guild, channel, hardcodedOnly).let { allReplacements ->
                allReplacements.asSequence()
                        .flatMap { replacement ->
                            if (!replacement.matchPhrase.endsWith("s")
                                    && allReplacements.none { it.matchPhrase == (replacement.matchPhrase + "s") }) {
                                sequenceOf(
                                        replacement.matchPhrase to replacement.replacementPhrase,
                                        replacement.matchPhrase + "s" to replacement.replacementPhrase + "s"
                                )
                            } else {
                                sequenceOf(replacement.matchPhrase to replacement.replacementPhrase)
                            }
                        }
                        .sortedByDescending { (phrase, _) -> phrase.length }
                        .map { (phrase, replacement) -> "(\\b$phrase|$phrase\\b)" to replacement }
                        .map { (patternString, replacementPhrase) ->
                            Pattern.compile(patternString, Pattern.CASE_INSENSITIVE) to replacementPhrase
                        }
                        .toList()
            }

    private fun getReplacementsForGuildAndChannel(guild: Guild, channel: TextChannel?, hardcodedOnly: Boolean) =
            // TODO: If in channel, only get enabled groups
            if (hardcodedOnly) HardcodedCensorshipReplacements.list
            else HardcodedCensorshipReplacements.list.plus(censorRepo.findAllByGuildId(guild.idLong))

    private fun getAllReplacementsForGuild(guild: Guild) = HardcodedCensorshipReplacements.list
            .plus(censorRepo.findAllByGuildId(guild.idLong))

    fun saveReplacement(guild: Guild, group: String, matchPhrase: String, replacementPhrase: String) {
        val existing = censorRepo.findByGuildIdAndGroupKeyAndMatchPhrase(guild.idLong, group, matchPhrase)
        val replacement = CensorshipReplacement(
                id = existing?.id,
                guildId = guild.idLong,
                groupKey = group,
                matchPhrase = matchPhrase,
                replacementPhrase = replacementPhrase
        )
        censorRepo.save(replacement)
    }

    fun removeReplacement(guild: Guild, group: String, matchPhrase: String): Boolean {
        val existing = censorRepo.findByGuildIdAndGroupKeyAndMatchPhrase(guild.idLong, group, matchPhrase)
        if (existing != null) censorRepo.delete(existing)
        return existing != null
    }
}