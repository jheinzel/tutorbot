package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper
import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper.FeedbackCount
import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper.Review
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.printlnCyan
import at.fhooe.hagenberg.tutorbot.util.printlnGreen
import at.fhooe.hagenberg.tutorbot.util.promptBooleanInput
import picocli.CommandLine.Command
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
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
    private fun addIfAllowed(review: Review, chosenReviews: MutableCollection<Review>): Boolean =
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
     */
    private fun pickReviewsToFeedback(
        reviews: MutableSet<Review>,
        feedbackCsv: File,
        feedbackCount: Int,
        randomCount: Int
    ): Set<Review> {
        val feedbackCountMap = try {
            feedbackHelper.readFeedbackCountFromCsv(feedbackCsv)
        } catch (e: FileNotFoundException) {
            mapOf<String, FeedbackCount>() // If file does not exist, just return empty map
        } catch (e: Exception) {
            exitWithError(e.message ?: "Parsing feedback CSV failed.")
        }
        val chosenReviews = mutableSetOf<Review>()
        val canStillPickReviews = { reviews.isNotEmpty() && chosenReviews.size < feedbackCount }

        // 1) Chose random reviews first
        while (canStillPickReviews() && chosenReviews.size < randomCount) {
            val review = reviews.random(random).also { reviews.remove(it) }
            addIfAllowed(review, chosenReviews)
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
            val student = studentsWithNoFeedback.first()
            studentsWithNoFeedback.remove(student)
            tryPopReviewForStudent(student, reviews, true)?.also {
                addIfAllowed(it, chosenReviews)
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
            val picked = feedbackOrdering.first()
            feedbackOrdering.remove(picked)

            // Try to keep amount of feedbacks on submissions and reviews the same
            val shouldPickSubmission = picked.second.submission <= picked.second.review
            tryPopReviewForStudent(picked.first, reviews, shouldPickSubmission)?.also {
                addIfAllowed(it, chosenReviews)
            }
        }

        return chosenReviews
    }

    override fun execute() {
        // Current ex. reviews path
        val baseDir = configHandler.getBaseDir()
        val exerciseSubDir = configHandler.getExerciseSubDir()
        val reviewsDir = configHandler.getReviewsSubDir()
        val sourceDirectory = Path.of(baseDir, exerciseSubDir, reviewsDir)
        val reviews = feedbackHelper.readAllReviewsFromDir(sourceDirectory.toFile())
        if (reviews.isEmpty()) exitWithError("Reviews folder does not contain any valid files!")

        // Get CSV with count of previous feedbacks
        val feedbackDirPath = configHandler.getFeedbackCsv()
        val feedbackCsv = Path.of(feedbackDirPath).toFile()

        // Count arguments
        val feedbackCount = configHandler.getFeedbackAmount()
        if (feedbackCount < 1) exitWithError("Feedback count must at least be 1.")
        val randomCount = configHandler.getFeedbackRandomAmount()
        if (randomCount < 0 || randomCount > feedbackCount) exitWithError("Random feedback count must be >= 0 and <= feedback count.")

        // Pick reviews
        val reviewsToFeedback =
            pickReviewsToFeedback(reviews.toMutableSet(), feedbackCsv, feedbackCount, randomCount)
        val reviewsToMove = reviews - reviewsToFeedback

        if (reviewsToFeedback.size != feedbackCount)
            printlnCyan("Could only pick ${reviewsToFeedback.size}/$feedbackCount reviews.")
        else
            printlnGreen("Picked $feedbackCount reviews.")

        val targetDirectory = sourceDirectory.resolve(NOT_SELECTED_DIR)
        if (!targetDirectory.toFile().mkdir()) {
            if (!promptBooleanInput("Target location $targetDirectory already exists, should its contents be overwritten?")) {
                exitWithError("Cancelled feedback choosing.")
            }
        }

        // Move reviews not selected for feedback in subfolder
        for (rev in reviewsToMove) {
            try {
                sourceDirectory.resolve(rev.fileName).toFile().run {
                    copyTo(targetDirectory.resolve(rev.fileName).toFile(), true)
                    delete()
                }
            } catch (ex: Exception) {
                exitWithError(ex.message ?: "Moving ${rev.fileName} failed.")
            }
        }

        printlnGreen("Finished selecting reviews to feedback.")
    }

    companion object {
        const val NOT_SELECTED_DIR = "not-selected"
    }
}
