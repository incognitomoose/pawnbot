package net.shelg.pawnbot.pornhub

import net.shelg.pawnbot.configuration.ConfigService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.random.Random


@Service
class PornhubCommentPrecacher(
        private val configService: ConfigService,
        private val pornhubClient: PornhubClient,
        private val commentRepo: PornhubCommentRepository,
        private val videoRepo: PornhubVideoRepository
) {
    private fun getRandomIntBetween(min: Int, max: Int) = min + Random.nextInt(max + 1 - min)

    @Scheduled(cron = "0/30 * * * * ?")
    fun scheduled() {
        var numVidsFetched = 0
        val allCommentNumWordsIntervals = configService.getAllCommentNumWordsIntervals()
        for ((minWords, maxWords) in allCommentNumWordsIntervals) {
            numVidsFetched += ensureNumUnusedCommentsWithinTarget(
                    TARGET_UNUSED_COMMENTS,
                    gay = true,
                    minWords = minWords,
                    maxWords = maxWords,
                    maxVids = MAX_VIDS_PER_RUN - numVidsFetched
            )
            if (numVidsFetched >= MAX_VIDS_PER_RUN) {
                LOGGER.info("Reached limit of $numVidsFetched videos per scheduled run")
                break
            }

            numVidsFetched += ensureNumUnusedCommentsWithinTarget(
                    TARGET_UNUSED_COMMENTS,
                    gay = false,
                    minWords = minWords,
                    maxWords = maxWords,
                    maxVids = MAX_VIDS_PER_RUN - numVidsFetched
            )
            if (numVidsFetched >= MAX_VIDS_PER_RUN) {
                LOGGER.info("Reached limit of $numVidsFetched videos per scheduled run")
                break
            }
        }
    }

    private fun ensureNumUnusedCommentsWithinTarget(targetNumUnusedComments: Int, gay: Boolean, minWords: Int, maxWords: Int, maxVids: Int) =
            fetchComments(
                    commentsNeeded = targetNumUnusedComments - getNumUnusedComments(gay, minWords, maxWords),
                    gay = gay,
                    minWords = minWords,
                    maxWords = maxWords,
                    maxVids = maxVids
            )

    private fun getNumUnusedComments(gay: Boolean, minWords: Int, maxWords: Int) =
            commentRepo.countByVideoGayEqualsAndNumWordsBetweenAndNumUsagesEquals(
                    gay = gay,
                    minWords = minWords,
                    maxWords = maxWords,
                    numUsages = 0
            )

    private fun fetchComments(commentsNeeded: Long, gay: Boolean, minWords: Int, maxWords: Int, maxVids: Int): Int {
        var numVidsFetched = 0
        if (commentsNeeded > 0 && maxVids > 0) {
            LOGGER.info("Fetching up to $commentsNeeded ${if (gay) "gay" else "straight"} comments with" +
                    " $minWords to $maxWords words...")
            var numComments = 0
            while (numComments < commentsNeeded && numVidsFetched < maxVids) {
                val video = pornhubClient.getRandomVideo(gay = gay)
                numVidsFetched++
                if (video.comments.isEmpty()) {
                    LOGGER.info("Not saving ${video.summarized}")
                } else {
                    LOGGER.info("Saving ${video.summarized}")
                    videoRepo.save(video)
                    numComments += video.comments.filter { it.numWords in minWords..maxWords }.size
                }
                waitMillisecondsSafe(getRandomIntBetween(1000, 3000))
            }
        }
        return numVidsFetched
    }

    private fun waitMillisecondsSafe(milliseconds: Int) {
        try {
            LOGGER.info("Waiting ${milliseconds}ms...")
            Thread.sleep(milliseconds.toLong())
        } catch (e: InterruptedException) {
            LOGGER.error("Interrupted while waiting!", e)
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(PornhubCommentPrecacher::class.java)

        const val TARGET_UNUSED_COMMENTS = 20
        const val MAX_VIDS_PER_RUN = 10
    }
}
