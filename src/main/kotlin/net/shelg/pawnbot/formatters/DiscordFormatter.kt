package net.shelg.pawnbot.formatters

object DiscordFormatter {
    fun bold(text: String) = "**$text**"
    fun italics(text: String) = "*$text*"
    fun quote(text: String) = text.split("\n").joinToString(separator = "\n", postfix = "\n") { "> $it" }
    fun underline(text: String) = "__${text}__"
}