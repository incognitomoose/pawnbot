package net.shelg.pawnbot.censoring

object HardcodedCensorshipReplacements {
    val list = listOf(
            censor("cracker", "cr***er".replace("*", "\\*")),
            censor("nigga", "n****".replace("*", "\\*")),
            censor("nigger", "n*****".replace("*", "\\*")),
            censor("kike", "k***".replace("*", "\\*")),
            censor("chink", "ch***".replace("*", "\\*")),
            censor("spic", "sp**".replace("*", "\\*")),
            censor("dego", "d***".replace("*", "\\*")),
            censor("faggot", "f*****".replace("*", "\\*")),
            censor("fag", "f**".replace("*", "\\*"))
    )

    private fun censor(phrase: String, replacement: String) =
            CensorshipReplacement(
                    guildId = 0L,
                    groupKey = "hardcoded",
                    matchPhrase = phrase,
                    replacementPhrase = replacement
            )
}