package net.shelg.pawnbot.pornhub

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Service

@Service
class PornhubClient(buildProperties: BuildProperties) {
    private val userAgent = "$BOT_NAME/${buildProperties.version} (+$BOT_URL)"

    private fun getMetaProperty(document: Document, propertyName: String): String {
        return document.selectFirst("meta[property='$propertyName']").attr("content")
    }

    fun getRandomVideo(gay: Boolean) =
            toPornhubVideoWithComments(gay, getRandomPage(gay))

    private fun toPornhubVideoWithComments(gay: Boolean, document: Document) =
            toPornhubVideo(gay, document)
                    .also { it.comments = parseComments(document, it) }

    private fun toPornhubVideo(gay: Boolean, document: Document) =
            PornhubVideo(
                    gay = gay,
                    title = getMetaProperty(document, "og:title"),
                    url = getMetaProperty(document, "og:url")
            )

    private fun parseComments(document: Document, video: PornhubVideo) =
            document.select("#cmtContent > .commentBlock")
                    .asSequence()
                    .filter { commentBlock ->
                        selectTextInCommentBlock(commentBlock)
                                .let {
                                    it.isNotBlank() // Should not be empty
                                            && !it.contains("http") // Should not contain a link
                                }
                    }
                    .map { commentBlock ->
                        val user = selectUserInCommentBlock(commentBlock)
                        val text = selectTextInCommentBlock(commentBlock)
                        toPornhubComment(text.trim { it <= ' ' }, user, video)
                    }
                    .toList()

    private fun selectTextInCommentBlock(commentBlock: Element) =
            commentBlock.selectFirst(".commentMessage > span").text()

    private fun selectUserInCommentBlock(commentBlock: Element) =
            commentBlock.selectFirst(".usernameLink").text()

    private fun toPornhubComment(text: String, user: String, video: PornhubVideo) =
            PornhubComment(
                    text = text,
                    user = user,
                    video = video,
                    numWords = text.split(Regex("\\s+")).size,
                    numUsages = 0
            )

    private fun getRandomPage(gay: Boolean): Document {
        val url = if (gay) URL_RANDOM_VIDEO_GAY else URL_RANDOM_VIDEO_STRAIGHT
        LOGGER.info("HTTP GET $url")
        return Jsoup.connect(url).userAgent(userAgent).get()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PornhubClient::class.java)
        private const val URL_RANDOM_VIDEO_GAY = "https://www.pornhub.com/gay/video/random"
        private const val URL_RANDOM_VIDEO_STRAIGHT = "https://www.pornhub.com/video/random"
        private const val BOT_NAME = "Pawnbot"
        private const val BOT_URL = "https://github.com/incognitomoose/pawnbot"
    }
}