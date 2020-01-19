package net.shelg.pawnbot.pornhub

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import kotlin.random.Random


@Service
class PornhubCommentPrecacher(
        private val pornhubClient: PornhubClient,
        private val commentRepo: PornhubCommentRepository,
        private val videoRepo: PornhubVideoRepository
) {
    private fun getRandomIntBetween(min: Int, max: Int) = min + Random.nextInt(max + 1 - min)

    @Scheduled(cron = "0/30 * * * * ?")
    fun scheduled() {
        getNewVideosAsNeeded(gay = false)
        getNewVideosAsNeeded(gay = true)
    }

    private fun getNewVideosAsNeeded(gay: Boolean) =
            numCommentsNeeded(gay).takeIf { it > 0 }
                    ?.let { getNewRandomVideos(numNeeded = it, gay = gay) }

    private fun numCommentsNeeded(gay: Boolean) = (TARGET_UNUSED_COMMENTS
            - commentRepo.countByVideoGayEqualsAndNumUsagesEquals(gay, 0))

    private fun getNewRandomVideos(numNeeded: Long, gay: Boolean) {
        LOGGER.info("Fetching up to $numNeeded ${if (gay) "gay" else "straight"} comments...")
        var numVids = 0
        var numGotten = 0
        while (numGotten < numNeeded) {
            val video = pornhubClient.getRandomVideo(gay = gay)
            waitMillisecondsSafe(getRandomIntBetween(1000, 3000))
            if (video.comments.isEmpty()) {
                LOGGER.info("Not saving ${video.summarized}")
            } else {
                LOGGER.info("Saving ${video.summarized}")
                videoRepo.save(video)
                numGotten += video.comments.size
            }
            numVids++
            if (numVids == MAX_VIDS_PER_RUN) {
                LOGGER.info("Reached limit of $numVids videos per scheduled run")
                break
            }
        }
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

        const val TARGET_UNUSED_COMMENTS = 200
        const val MAX_VIDS_PER_RUN = 10
    }
}
