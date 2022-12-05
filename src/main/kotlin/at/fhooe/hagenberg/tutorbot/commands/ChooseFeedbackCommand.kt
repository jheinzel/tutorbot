package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper
import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper.Review
import at.fhooe.hagenberg.tutorbot.util.*
import picocli.CommandLine.Command
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import javax.inject.Inject
import kotlin.random.Random


@Command(
    name = "choose-feedback",
    description = ["Choose reviews to give feedback on. Students who have gotten less feedbacks on submissions or reviews have a greater chance of being picked."]
)
class ChooseFeedbackCommand @Inject constructor(
    private val configHandler: ConfigHandler,
    private val feedbackHelper: FeedbackHelper,
    private val random: Random
) : BaseCommand() {
    /**
     * Adds the review to the chosenReviews if both participating students are not in the chosenReviews already.
     * @return true if added
     */
    private fun addIfFeedbackAllowed(review: Review, chosenReviews: MutableCollection<Review>): Boolean =
        if (chosenReviews.none { r ->
                r.subStudentNr == review.revStudentNr || r.subStudentNr == review.subStudentNr
                        || r.revStudentNr == review.revStudentNr || r.revStudentNr == review.subStudentNr
            }) chosenReviews.add(review) else false

    /**
     *  First try to add review where student is submitter, if not possible try reviewer.
     *  Also removes entries from the input reviews.
     */
    private fun tryAddReviewAsSubmitterOrReviewer(
        student: String,
        reviews: MutableCollection<Review>,
        chosenReviews: MutableCollection<Review>
    ) {
        if (reviews.firstOrNull { r -> r.subStudentNr == student }?.let {
                reviews.remove(it)
                addIfFeedbackAllowed(it, chosenReviews)
            } != true) {
            reviews.firstOrNull { r -> r.revStudentNr == student }?.let {
                reviews.remove(it)
                addIfFeedbackAllowed(it, chosenReviews)
            }
        }
    }

    /**
     * Chooses reviews to be selected for feedback. Students who have gotten the least feedbacks on submissions and reviews are preferred.
     * Random reviews regardless of their received feedback amount may also be chosen if defined (useful to avoid predictability).
     */
    private fun pickReviewsToFeedback(reviews: MutableSet<Review>, feedbackCount: Int, randomCount: Int): Set<Review> {
        if (reviews.isEmpty()) exitWithError("Reviews folder is empty!")
        val feedbackCountMap = feedbackHelper.readFeedbackCountForStudents()
        val chosenReviews = mutableSetOf<Review>()

        val canStillPickReviews = { reviews.isNotEmpty() && chosenReviews.size < feedbackCount }

        // 1) Chose random reviews first
        while (canStillPickReviews() && chosenReviews.size < randomCount) {
            val review = reviews.random().also { reviews.remove(it) }
            addIfFeedbackAllowed(review, chosenReviews)
        }

        if (!canStillPickReviews()) return chosenReviews

        // 2) Choose students who have not gotten any feedback, does not matter if feedback on submission or review
        val studentsWithNoFeedback = reviews
            .flatMap { r -> listOf(r.subStudentNr, r.revStudentNr) }
            .distinct()
            .filter { s -> s !in feedbackCountMap }
            .shuffled(random)
            .toMutableList()
        while (canStillPickReviews() && studentsWithNoFeedback.isNotEmpty()) {
            studentsWithNoFeedback.first().also {
                studentsWithNoFeedback.remove(it)
                tryAddReviewAsSubmitterOrReviewer(it, reviews, chosenReviews)
            }
        }

        if (!canStillPickReviews()) return chosenReviews

        // 3) Choose students by ordering amount of feedback received
        val feedbackOrdering = feedbackCountMap
            .toList()
            .filter { pair -> pair.first in reviews.flatMap { r -> listOf(r.subStudentNr, r.revStudentNr) } }
            .shuffled(random)
            .sortedBy { pair -> pair.second }
            .toMutableList()
        while (canStillPickReviews() && feedbackOrdering.isNotEmpty()) {
            feedbackOrdering.first().also {
                feedbackOrdering.remove(it)
                tryAddReviewAsSubmitterOrReviewer(it.first, reviews, chosenReviews)
            }
        }

        return chosenReviews
    }

    override fun execute() {
        val feedbackCount =
            configHandler.getFeedbackAmount() ?: promptNumberInput("Enter amount of reviews to pick:")
        if (feedbackCount < 1) exitWithError("Feedback count must at least be 1.")
        val randomCount =
            configHandler.getFeedbackRandomAmount()
                ?: promptNumberInput("Enter amount of random reviews to pick (0 <= amount <= $feedbackCount):")
        if (randomCount < 0 || randomCount > feedbackCount) exitWithError("Random feedback count must be >= 0 and <= feedback count.")


        val reviews = feedbackHelper.readReviewsForExercise()
        val reviewsToFeedback = pickReviewsToFeedback(reviews.toMutableSet(), feedbackCount, randomCount)
        val reviewsToMove = reviews - reviewsToFeedback

        if (reviewsToFeedback.size != feedbackCount)
            printlnCyan("Could only pick ${reviewsToFeedback.size}/$feedbackCount reviews to avoid overlapping.")
        else
            printlnGreen("Successfully selected $feedbackCount reviews.")

        val sourceDirectory =
            Path.of(configHandler.getBaseDir()!!, configHandler.getExerciseSubDir(), configHandler.getReviewsSubDir())
        val targetDirectory = sourceDirectory.resolve(NOT_SELECTED_DIR)
        if (!targetDirectory.toFile().mkdir()) {
            if (!promptBooleanInput("Target location $targetDirectory already exists, should its contents be overwritten?")) {
                exitWithError("Cancelled feedback choosing.")
            }
        }

        // Move reviews not selected to feedback in subfolder
        for (rev in reviewsToMove) {
            try {
                Files.move(
                    sourceDirectory.resolve(rev.fileName),
                    targetDirectory.resolve(rev.fileName),
                    StandardCopyOption.REPLACE_EXISTING
                )

            } catch (ex: Exception) {
                exitWithError(ex.message ?: "Moving files failed with $rev.")
            }
        }
    }

    private companion object {
        const val NOT_SELECTED_DIR = "not-selected"
    }
}
