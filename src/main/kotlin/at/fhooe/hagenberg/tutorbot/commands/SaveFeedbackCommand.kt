package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper
import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper.FeedbackCount
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.printlnGreen
import picocli.CommandLine.Command
import java.io.FileNotFoundException
import java.nio.file.Path
import javax.inject.Inject

@Command(
    name = "save-feedback",
    description = ["Reads the feedback amount from an exercise and appends it to the feedback CSV file."]
)
class SaveFeedbackCommand @Inject constructor(
    private val configHandler: ConfigHandler,
    private val feedbackHelper: FeedbackHelper
) : BaseCommand() {
    override fun execute() {
        // Current ex. reviews path
        val baseDir = configHandler.getBaseDir()
        val exerciseSubDir = configHandler.getExerciseSubDir()
        val reviewsDir = configHandler.getReviewsSubDir()
        val sourceDirectory = Path.of(baseDir, exerciseSubDir, reviewsDir)
        val feedbackCount = feedbackHelper.readFeedbackCountFromReviews(sourceDirectory.toFile())
        if (feedbackCount.isEmpty()) exitWithError("Reviews folder does not contain any valid files!")

        // Get CSV with count of previous feedbacks
        val feedbackDirPath = configHandler.getFeedbackCsv()
        val feedbackCsv = Path.of(feedbackDirPath).toFile()
        val existingFeedbackCount = try {
            feedbackHelper.readFeedbackCountFromCsv(feedbackCsv)
        } catch (e: FileNotFoundException) {
            mapOf<String, FeedbackCount>() // If file does not exist, just return empty map
        } catch (e: Exception) {
            exitWithError(e.message ?: "Parsing feedback CSV failed.")
        }

        val mergedCount = (existingFeedbackCount.asSequence() + feedbackCount.asSequence())
            .groupBy({ it.key }, { it.value })
            .mapValues { (_, counts) ->
                counts.reduce { acc, f ->
                    FeedbackCount(
                        acc.submission + f.submission,
                        acc.review + f.review
                    )
                }
            }

        try {
            feedbackHelper.writeFeedbackCountToCsv(feedbackCsv, mergedCount)
            printlnGreen("Successfully wrote new feedback count to ${feedbackCsv.absolutePath}")
        } catch (e: Exception){
            exitWithError(e.message ?: "Could not write to ${feedbackCsv.absolutePath}")
        }
    }
}
