package net.shelg.pawnbot.censoring.commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.censoring.CensorshipReplacement
import net.shelg.pawnbot.censoring.CensorshipReplacementService
import net.shelg.pawnbot.formatters.DiscordFormatter
import net.shelg.pawnbot.formatters.DiscordFormatter.italics
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
        val response = respond(args["group"], context.textChannel)
        textSender.sendMessage(response, context.textChannel)
    }

    private fun respond(group: String?, channel: TextChannel) =
            if (group != null) {
                replacementService.getReplacementsInGroup(channel, group)
                        .takeIf { it.isNotEmpty() }?.let(::listGroupCensorships)
                        ?: "Group $group does not exist.\n${listGroups(channel)}"
            } else {
                listGroups(channel)
            }

    private fun listGroups(channel: TextChannel) =
            replacementService.getAllGroups(channel).let { allGroups ->
                if (allGroups.isEmpty()) {
                    "No phrases to censor are defined."
                } else {
                    "Available groups: ${allGroups.joinToString(separator = ", ")}\n" +
                            "Use list ${italics("<group>")} to show replacements."
                }
            }

    private fun listGroupCensorships(replacement: List<CensorshipReplacement>) =
            "${DiscordFormatter.bold("Replacements in group ${replacement.first().groupKey}:")}\n" +
                    replacement.joinToString(separator = "\n") { it.matchPhrase + " -> " + it.replacementPhrase }

}