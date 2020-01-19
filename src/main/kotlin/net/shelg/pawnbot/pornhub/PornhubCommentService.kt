package net.shelg.pawnbot.pornhub

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import kotlin.random.Random


@Service
class PornhubCommentService(private val commentRepo: PornhubCommentRepository) {
    fun getRandomComment(percentGay: Int, minWords: Int, maxWords: Int): PornhubComment? {
        val gay = getRandomBooleanWithProbabilityPercentage(percentGay)
        val numAvailable = commentRepo.countByVideoGayEqualsAndNumWordsBetweenAndNumUsagesEquals(
                gay = gay,
                minWords = minWords,
                maxWords = maxWords,
                numUsages = 0
        ).toInt()
        return if (numAvailable > 0) {
            commentRepo.findByVideoGayEqualsAndNumWordsBetweenAndNumUsagesEquals(
                    gay = gay,
                    minWords = minWords,
                    maxWords = maxWords,
                    numUsages = 0,
                    pageable = PageRequest.of(Random.nextInt(numAvailable), 1)
            ).firstOrNull()
        } else {
            null
        }
    }

    private fun getRandomBooleanWithProbabilityPercentage(probability: Int) =
            when {
                probability <= 0 -> false
                probability >= 100 -> true
                else -> Random.nextInt(100) < probability
            }
}