package net.shelg.pawnbot

import java.util.regex.Matcher
import java.util.regex.Pattern

fun String.replaceAllMatches(pattern: Pattern, replacementFunction: (Matcher) -> String): String {
    val matcher = pattern.matcher(this)
    val builder = StringBuilder()
    while (matcher.find()) {
        matcher.appendReplacement(builder, replacementFunction.invoke(matcher))
    }
    matcher.appendTail(builder)
    return builder.toString()
}
