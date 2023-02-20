package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.FeedbackChooseLogic
import at.fhooe.hagenberg.tutorbot.components.FeedbackFileHelper
import at.fhooe.hagenberg.tutorbot.domain.FeedbackCount
import at.fhooe.hagenberg.tutorbot.domain.Review
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.printlnCyan
import at.fhooe.hagenberg.tutorbot.util.printlnGreen
import at.fhooe.hagenberg.tutorbot.util.promptBooleanInput
import picocli.CommandLine.Command
import java.io.FileNotFoundException
import java.nio.file.Path
import javax.inject.Inject

@Command(
    name = "choose-feedback",
    description = ["Choose reviews to give feedback on. Students who have gotten less feedbacks on submissions or reviews have a greater chance of being picked."]
)
class ChooseFeedbackCommand @Inject constructor(
    private val configHandler: ConfigHandler,
    private val feedbackFileHelper: FeedbackFileHelper,
    private val feedbackChooseLogic: FeedbackChooseLogic,
    private val saveFeedbackCommand: SaveFeedbackCommand
) : BaseCommand() {
    override fun execute() {
        // Current ex. reviews path
        val baseDir = configHandler.getBaseDir()
        val exerciseSubDir = configHandler.getExerciseSubDir()
        val reviewsDir = configHandler.getReviewsSubDir()
        val sourceDirectory = Path.of(baseDir, exerciseSubDir, reviewsDir)
        val reviews = feedbackFileHelper.readAllReviewsFromDir(sourceDirectory.toFile())
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
        val feedbackCountMap = try {
            feedbackFileHelper.readFeedbackCountFromCsv(feedbackCsv)
        } catch (e: FileNotFoundException) {
            mapOf<String, FeedbackCount>() // If file does not exist, just return empty map
        } catch (e: Exception) {
            exitWithError(e.message ?: "Parsing feedback CSV failed.")
        }
        val reviewsToFeedback =
            feedbackChooseLogic.pickReviewsToFeedback(feedbackCountMap, reviews, feedbackCount, randomCount)
        val reviewsToMove = reviews - reviewsToFeedback

        if (reviewsToFeedback.size != feedbackCount)
            printlnCyan("Could only pick ${reviewsToFeedback.size}/$feedbackCount reviews.")
        else
            printlnGreen("Picked $feedbackCount reviews.")

        moveReviews(sourceDirectory, reviewsToMove)
        printlnGreen("Finished selecting reviews to feedback.")

        if (promptBooleanInput("Save current feedback selection?")) {
            saveFeedbackCommand.execute()
        } else {
            printlnCyan("Feedback selection was not saved. Please save your selection with save-feedback when you are done.")
        }
    }

    private fun moveReviews(
        sourceDirectory: Path,
        reviewsToMove: Set<Review>
    ) {
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
    }

    companion object {
        const val NOT_SELECTED_DIR = "not-selected"
    }
}
