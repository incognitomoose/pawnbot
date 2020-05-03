package net.shelg.pawnbot.censoring.commands

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.censoring.CensorshipReplacementService
import net.shelg.pawnbot.triggers.commands.AbstractChatSubcommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import org.springframework.stereotype.Service

@Service
class CensorAddChatSubcommand(
        private val replacementService: CensorshipReplacementService,
        private val textSender: TextSender,
        cliParser: CommandLineParser
) : AbstractChatSubcommand(cliParser, textSender) {

    override fun commandSyntax() = "add {group} {match_phrase} -> {replacement_phrase}"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = respond(
                group = args.getValue("group").toLowerCase(),
                matchPhrase = args.getValue("match_phrase").toLowerCase(),
                replacementPhrase = args.getValue("replacement_phrase"),
                textChannel = context.textChannel
        )
        textSender.sendMessage(response, context.textChannel)
    }

    private fun respond(group: String, matchPhrase: String, replacementPhrase: String, textChannel: TextChannel) =
            replacementService.saveReplacement(
                    textChannel = textChannel,
                    group = group,
                    matchPhrase = matchPhrase,
                    replacementPhrase = replacementPhrase
            ).let { "Replacement added to group $group" }
}