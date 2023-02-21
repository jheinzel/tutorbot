package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.domain.FeedbackCount
import at.fhooe.hagenberg.tutorbot.domain.Review
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Logic behind choosing feedback from a list of reviews.
 * Criteria: The students with the least amount of feedback should be chosen.
 * But no student should be chosen twice for the same exercise (as reviewer and submitter).
 */
@Singleton
class FeedbackChooseLogic @Inject constructor(private val random: Random) {
    /**
     * Adds the review to the chosenReviews if both participating students are not in the chosenReviews already.
     * @return true if added
     */
    private fun addIfAllowed(
        review: Review,
        chosenReviews: MutableCollection<Review>
    ): Boolean =
        if (chosenReviews.none { r ->
                r.subStudentNr == review.revStudentNr || r.subStudentNr == review.subStudentNr
                        || r.revStudentNr == review.revStudentNr || r.revStudentNr == review.subStudentNr
            }) chosenReviews.add(review) else false

    /**
     * Tries to pop a Review from the reviews, which either has the student as submitter or reviewer.
     * @return Review if present in reviews
     */
    private fun tryPopReviewForStudent(
        student: String,
        reviews: MutableCollection<Review>,
        asSubmitter: Boolean
    ): Review? =
        when (asSubmitter) {
            true -> reviews.firstOrNull { r -> r.subStudentNr == student }
            false -> reviews.firstOrNull { r -> r.revStudentNr == student }
        }?.also { reviews.remove(it) }

    /**
     * Chooses reviews to be selected for feedback. Students who have gotten the least feedbacks on submissions and reviews are preferred.
     * Random reviews regardless of their received feedback amount may also be chosen if defined (useful to avoid predictability).
     * @param feedbackCountMap number of feedbacks a student has received on submissions and reviews
     * @param reviewsLeft reviews to choose from
     * @param feedbackCount amount of reviews to choose
     * @param randomCount amount of random reviews to choose with no regard to feedback count
     * @return List of chosen reviews
     */
    fun pickReviewsToFeedback(
        feedbackCountMap: Map<String, FeedbackCount>,
        reviews: Set<Review>,
        feedbackCount: Int,
        randomCount: Int
    ): Set<Review> {
        val chosenReviews = mutableSetOf<Review>()
        val reviewsLeft = reviews.toMutableSet()
        val canStillPickReviews = { reviewsLeft.isNotEmpty() && chosenReviews.size < feedbackCount }

        // 1) Chose random reviews first
        while (canStillPickReviews() && chosenReviews.size < randomCount) {
            val review = reviewsLeft.random(random).also { reviewsLeft.remove(it) }
            addIfAllowed(review, chosenReviews)
        }

        if (!canStillPickReviews()) return chosenReviews

        // 2) Choose students who have not gotten any feedback, does not matter if feedback on submission or review
        val studentsWithNoFeedback = reviewsLeft
            .flatMap { r -> listOf(r.subStudentNr, r.revStudentNr) }
            .distinct()
            .filter { s -> s !in feedbackCountMap }
            .shuffled(random)
            .toMutableList()
        while (canStillPickReviews() && studentsWithNoFeedback.isNotEmpty()) {
            val student = studentsWithNoFeedback.first()
            studentsWithNoFeedback.remove(student)
            tryPopReviewForStudent(student, reviewsLeft, true)?.also {
                addIfAllowed(it, chosenReviews)
            }
        }

        if (!canStillPickReviews()) return chosenReviews

        // 3) Choose students by ordering amount of feedback received
        val feedbackOrdering = feedbackCountMap
            .toList()
            .filter { pair -> pair.first in reviewsLeft.flatMap { r -> listOf(r.subStudentNr, r.revStudentNr) } }
            .shuffled(random)
            .sortedBy { pair -> pair.second }
            .toMutableList()
        while (canStillPickReviews() && feedbackOrdering.isNotEmpty()) {
            val picked = feedbackOrdering.first()
            feedbackOrdering.remove(picked)

            // Try to keep amount of feedbacks on submissions and reviews the same
            val shouldPickSubmission = picked.second.submission <= picked.second.review
            tryPopReviewForStudent(picked.first, reviewsLeft, shouldPickSubmission)?.also {
                addIfAllowed(it, chosenReviews)
            }
        }

        return chosenReviews
    }
}