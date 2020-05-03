package net.shelg.pawnbot.reddit

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.shelg.pawnbot.TextSender
import net.shelg.pawnbot.censoring.Censor
import net.shelg.pawnbot.configuration.ConfigService
import net.shelg.pawnbot.events.EventHub
import net.shelg.pawnbot.pornhub.RedditClient
import net.shelg.pawnbot.triggers.commands.AbstractChatCommand
import net.shelg.pawnbot.triggers.commands.CommandLineParser
import org.springframework.stereotype.Component

@Component
class RedditJokeCommand(
        cliParser: CommandLineParser,
        configService: ConfigService,
        private val eventHub: EventHub,
        private val textSender: TextSender,
        private val redditClient: RedditClient,
        private val censor: Censor
) : AbstractChatCommand(cliParser, textSender, configService) {

    override fun commandSyntax() = "joke"
    override fun description() = "Display a random joke from Reddit"

    override fun execute(args: Map<String, String>, context: Message) {
        textSender.startTyping(context.textChannel)
        val response = respond(context.textChannel)
        textSender.sendMessage(response, context.textChannel) {
            eventHub.fireSpeakableTextRelayed(response, context, it)
        }
    }

    private fun respond(channel: TextChannel) =
            censor.censorText(
                    text = redditClient.getRandomJoke().let { "${it.title}\n${it.text}" },
                    channel = channel
            )
}