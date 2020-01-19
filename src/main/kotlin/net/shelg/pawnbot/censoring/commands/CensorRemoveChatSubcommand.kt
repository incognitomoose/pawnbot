package net.shelg.pawnbot.censoring.commands

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.censoring.CensorshipReplacementService
import net.shelg.pawnbot.triggers.commands.AbstractChatSubcommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import org.springframework.stereotype.Service

@Service
class CensorRemoveChatSubcommand(
        private val replacementService: CensorshipReplacementService,
        private val textSender: TextSender,
        cliParser: CommandLineParser
) : AbstractChatSubcommand(cliParser, textSender) {

    override fun commandSyntax() = "remove {group} {match_phrase}"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = respond(
                group = args.getValue("group").toLowerCase(),
                matchPhrase = args.getValue("match_phrase").toLowerCase(),
                guild = context.guild
        )
        textSender.sendMessage(response, context.textChannel)
    }

    private fun respond(group: String, matchPhrase: String, guild: Guild) =
            when {
                group == "hardcoded" ->
                    "Cannot add to or remove from hardcoded group. You can override it by adding to a different group."
                replacementService.removeReplacement(guild, group, matchPhrase) ->
                    "Replacement removed from group $group"
                else ->
                    "No such replacement found in group $group"
            }

}