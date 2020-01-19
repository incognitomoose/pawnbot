package net.shelg.pawnbot.formatters

import java.time.Duration

object DurationFormatter {
    fun formatDuration(duration: Duration): String {
        val parts =
                sequenceOf(
                        formatUnit(duration.toDays(), "day"),
                        formatUnit(duration.toHours() % 24, "hour"),
                        formatUnit(duration.toMinutes() % 60, "minute"),
                        formatUnit(duration.seconds % 60, "second")
                ).filterNotNull().toList()

        val builder = StringBuilder()
        parts.withIndex().forEach { (i, part) ->
            if (builder.isNotEmpty()) builder.append(if (i == parts.size - 1) " and " else ", ")
            builder.append(part)
        }
        return builder.toString()
    }

    private fun formatUnit(amount: Long, unit: String) =
            when {
                amount >= 2L -> "$amount ${unit}s"
                amount == 1L -> "$amount $unit"
                else -> null
            }
}