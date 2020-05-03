package net.shelg.pawnbot.censoring

import net.dv8tion.jda.api.entities.TextChannel
import org.springframework.stereotype.Component
import java.util.regex.Matcher
import java.util.regex.Pattern

@Component
class Censor(private val replacementService: CensorshipReplacementService) {
    data class TextPart(val text: String, val censored: Boolean = false)

    fun censorText(text: String, channel: TextChannel) =
            replacementService.getRegexReplacements(channel)
                    .fold(listOf(TextPart(text)), ::applyReplacement)
                    .joinToString(separator = "", transform = TextPart::text)

    private fun applyReplacement(textParts: List<TextPart>, pair: Pair<Pattern, String>) =
            textParts.flatMap {
                if (it.censored) {
                    listOf(it)
                } else {
                    val uncensoredText = it.text

                    val matcher = pair.first.matcher(uncensoredText)

                    val partsAfterMatching = mutableListOf<TextPart>()
                    var previousEnd = 0

                    while (matcher.find()) {
                        val startIndex = matcher.start()
                        if (startIndex != previousEnd) {
                            partsAfterMatching += TextPart(
                                    text = uncensoredText.substring(previousEnd, startIndex),
                                    censored = false
                            )
                        }

                        partsAfterMatching += TextPart(
                                text = replacementText(pair, matcher),
                                censored = true
                        )

                        previousEnd = matcher.end()
                    }

                    if (previousEnd < uncensoredText.length) {
                        partsAfterMatching += TextPart(
                                text = uncensoredText.substring(previousEnd),
                                censored = false
                        )
                    }

                    partsAfterMatching
                }
            }

    private fun replacementText(replacementPair: Pair<Pattern, String>, matcher: Matcher) =
            replacementPair.second.toSameCaseAs(matcher.group(1))


    private fun String.toSameCaseAs(original: String) =
            if (original.hasLetters() && original.allLettersAreUppercase()) toUpperCase() else this

    private fun String.hasLetters() = any(Char::isLetter)
    private fun String.allLettersAreUppercase() = all { !it.isLetter() || it.isUpperCase() }
}