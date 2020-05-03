package net.shelg.pawnbot.censoring

import net.dv8tion.jda.api.entities.TextChannel
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service
class CensorshipReplacementService(private val censorRepo: CensorshipReplacementRepository) {
    fun getReplacementsInGroup(channel: TextChannel, group: String) =
            getReplacements(channel).filter { it.groupKey == group }

    fun getAllGroups(channel: TextChannel) =
            getReplacements(channel).asSequence().map(CensorshipReplacement::groupKey).distinct().sorted().toList()

    fun getRegexReplacements(channel: TextChannel) =
            getReplacements(channel).let { allReplacements ->
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

    private fun getReplacements(channel: TextChannel) =
            censorRepo.findAllByGuildId(channel.guild.idLong)

    fun saveReplacement(textChannel: TextChannel, group: String, matchPhrase: String, replacementPhrase: String) {
        val existing = censorRepo.findByGuildIdAndGroupKeyAndMatchPhrase(textChannel.guild.idLong, group, matchPhrase)
        val replacement = CensorshipReplacement(
                id = existing?.id,
                guildId = textChannel.guild.idLong,
                groupKey = group,
                matchPhrase = matchPhrase,
                replacementPhrase = replacementPhrase
        )
        censorRepo.save(replacement)
    }

    fun removeReplacement(channel: TextChannel, group: String, matchPhrase: String): Boolean {
        val existing = censorRepo.findByGuildIdAndGroupKeyAndMatchPhrase(channel.guild.idLong, group, matchPhrase)
        if (existing != null) censorRepo.delete(existing)
        return existing != null
    }
}