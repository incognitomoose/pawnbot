package net.shelg.pawnbot.triggers.commands

import org.springframework.stereotype.Service

@Service
class CommandLineParser {
    class LiteralPartMissing(val literal: String) : RuntimeException("Missing literal part \"$literal\"")
    class RequiredArgMissing(val argName: String) : RuntimeException("Missing argument  \"$argName\"")

    fun parseArgs(args: String?, argsPattern: String?): Map<String, String> {
        val map = mutableMapOf<String, String>()

        val patternParts = argsPattern?.split(' ')?.filter { it.isNotEmpty() } ?: listOf()
        var remainingArgs = args ?: ""

        for (patternPartIndex in patternParts.indices) {
            val patternPart = patternParts[patternPartIndex]
            if (isArg(patternPart)) {
                // Required arg
                val paramName = patternPart.substring(1, patternPart.length - 1)
                if (patternPartIndex == patternParts.size - 1) {
                    // Read end of command line into this arg
                    if (remainingArgs.isBlank()) {
                        if (isRequiredArg(patternPart)) {
                            throw RequiredArgMissing(paramName)
                        }
                    } else {
                        map[paramName] = remainingArgs.trim()
                    }
                } else {
                    // See if next param is arg
                    val paramValue = if (isArg(patternParts[patternPartIndex + 1])) {
                        val paramValueAndRest = remainingArgs.split(' ', limit = 2)
                        remainingArgs = paramValueAndRest.getOrNull(1)?.trim() ?: ""
                        paramValueAndRest[0]
                    } else {
                        val value = remainingArgs.substringBefore(patternParts[patternPartIndex + 1])
                        remainingArgs = remainingArgs.substringAfter(value)
                        value

                    }
                    if (paramValue.isBlank()) {
                        if (isRequiredArg(patternPart)) {
                            throw RequiredArgMissing(paramName)
                        }
                    } else {
                        map[paramName] = paramValue.trim()
                    }
                }
            } else {
                // Literal
                if (!remainingArgs.startsWith(patternPart)) {
                    throw LiteralPartMissing(patternPart)
                } else {
                    remainingArgs = remainingArgs.substringAfter(patternPart).trim()
                }
            }
        }

        return map
    }

    private fun isArg(patternPart: String) = isRequiredArg(patternPart) || isOptionalArg(patternPart)
    private fun isOptionalArg(patternPart: String) = patternPart.first() == '[' && patternPart.last() == ']'
    private fun isRequiredArg(patternPart: String) = patternPart.first() == '{' && patternPart.last() == '}'
}