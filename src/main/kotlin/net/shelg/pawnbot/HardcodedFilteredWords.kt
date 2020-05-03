package net.shelg.pawnbot

import java.util.regex.Pattern

class HardcodedFilter(replacementGenerator: (String) -> String) {
    fun apply(source: String) =
            regexes.fold(
                    initial = source,
                    operation = { text, (pattern, replacement) -> text.replaceAll(pattern, replacement) }
            )

    companion object {
        private val HARDCODED_FILTERED_WORDS_SINGULAR = listOf("cracker", "nigga", "nigger", "knee ger", "kneeger",
                "kike", "chink", "spic", "dego", "faggot", "fag")

        private val HARDCODED_FILTERED_WORDS =
                HARDCODED_FILTERED_WORDS_SINGULAR +
                        HARDCODED_FILTERED_WORDS_SINGULAR.map { "${it}s" }
    }

    private val regexes = HARDCODED_FILTERED_WORDS
            .sortedByDescending(String::length)
            .map { "(\\b$it|$it\\b)" to replacementGenerator.invoke(it) }
            .map { (patternString, replacementPhrase) ->
                Pattern.compile(patternString, Pattern.CASE_INSENSITIVE) to replacementPhrase
            }

    private fun String.replaceAll(pattern: Pattern, replacement: String) =
            pattern.matcher(this).let { matcher ->
                val builder = StringBuilder()
                while (matcher.find()) {
                    matcher.appendReplacement(builder, replacement)
                }
                matcher.appendTail(builder)
                builder.toString()
            }
}