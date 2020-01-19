package net.shelg.pawnbot.censoring.commands

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.censoring.CensorshipReplacement
import net.shelg.pawnbot.censoring.CensorshipReplacementService
import net.shelg.pawnbot.formatters.DiscordFormatter
import net.shelg.pawnbot.triggers.commands.AbstractChatSubcommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import org.springframework.stereotype.Service

@Service
class CensorListChatSubcommand(
        private val replacementService: CensorshipReplacementService,
        private val textSender: TextSender,
        cliParser: CommandLineParser
) : AbstractChatSubcommand(cliParser, textSender) {

    override fun commandSyntax() = "list [group]"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = respond(args["group"], context.guild)
        textSender.sendMessage(response, context.textChannel, hardcodedOnly = true)
    }

    private fun respond(group: String?, guild: Guild) =
            if (group != null) {
                replacementService.getReplacementsInGroup(guild, group)
                        .takeIf { it.isNotEmpty() }?.let(::listGroupCensorships)
                        ?: "Group $group does not exist.\n${listGroups(guild)}"
            } else {
                listGroups(guild)
            }

    private fun listGroups(guild: Guild) =
            "Available groups: ${replacementService.getAllGroups(guild).joinToString(separator = ", ")}\n" +
                    "Use list ${DiscordFormatter.italics("<group>")} to show replacements."

    private fun listGroupCensorships(replacement: List<CensorshipReplacement>) =
            "${DiscordFormatter.bold("Replacements in group ${replacement.first().groupKey}:")}\n" +
                    replacement.joinToString(separator = "\n") { it.matchPhrase + " -> " + it.replacementPhrase }

}