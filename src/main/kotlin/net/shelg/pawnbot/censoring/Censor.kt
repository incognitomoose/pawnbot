package net.shelg.pawnbot.censoring

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.replaceAllMatches
import org.springframework.stereotype.Component
import java.util.regex.Pattern

@Component
class Censor(private val replacementService: CensorshipReplacementService) {
    fun censorText(text: String, guild: Guild, channel: TextChannel? = null, hardcodedOnly: Boolean = false) =
            replacementService.getRegexReplacements(guild, channel, hardcodedOnly).fold(text, ::applyReplacement)

    private fun applyReplacement(text: String, pair: Pair<Pattern, String>) =
            text.replaceAllMatches(pair.first) {
                pair.second.toSameCaseAs(it.group(1)).replace("\\", "\\\\")
            }

    private fun String.toSameCaseAs(original: String) =
            if (original.hasLetters() && original.allLettersAreUppercase()) toUpperCase() else this

    private fun String.hasLetters() = any(Char::isLetter)
    private fun String.allLettersAreUppercase() = all { !it.isLetter() || it.isUpperCase() }
}