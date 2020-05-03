package net.shelg.pawnbot.pornhub

import net.dean.jraw.http.OkHttpNetworkAdapter
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.Credentials
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.references.SubredditReference
import net.shelg.pawnbot.Pawnbot
import net.shelg.pawnbot.reddit.RedditPost
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Service

@Service
class RedditClient(
        @Value("\${reddit.username}") redditUsername: String,
        @Value("\${reddit.password}") redditPassword: String,
        @Value("\${reddit.client.id}") redditClientId: String,
        @Value("\${reddit.client.secret}") redditClientSecret: String,
        buildProperties: BuildProperties
) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(RedditClient::class.java)
    }

    private val userAgent = UserAgent(
            platform = "bot",
            appId = Pawnbot::class.java.packageName,
            version = buildProperties.version,
            redditUsername = "incognitomoose2"
    )

    private val jokesSubreddit: SubredditReference by lazy {
        val credentials = Credentials.script(
                username = redditUsername,
                password = redditPassword,
                clientId = redditClientId,
                clientSecret = redditClientSecret
        )

        LOGGER.info("Connecting to Reddit with UserAgent \"${userAgent.value}\" and username ${credentials.username}")

        val reddit = OAuthHelper.automatic(OkHttpNetworkAdapter(userAgent), credentials)

        reddit.subreddit("Jokes")
    }

    fun getRandomJoke() =
            jokesSubreddit.randomSubmission().subject.let {
                LOGGER.info("Replying with random joke from " + it.url)
                RedditPost(title = it.title, text = it.selfText ?: "")
            }
}