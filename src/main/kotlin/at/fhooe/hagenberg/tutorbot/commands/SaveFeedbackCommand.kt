package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.FeedbackFileHelper
import at.fhooe.hagenberg.tutorbot.domain.FeedbackCount
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.printlnGreen
import picocli.CommandLine.Command
import java.io.FileNotFoundException
import java.nio.file.Path
import javax.inject.Inject

@Command(
    name = "save-feedback",
    description = ["Should only be run if the feedback count was not saved with choose-feedback. Counts the feedbacks in an exercise folder and updates the feedback CSV file."]
)
class SaveFeedbackCommand @Inject constructor(
    private val configHandler: ConfigHandler,
    private val feedbackFileHelper: FeedbackFileHelper
) : BaseCommand() {
    override fun execute() {
        // Current ex. reviews path
        val baseDir = configHandler.getBaseDir()
        val exerciseSubDir = configHandler.getExerciseSubDir()
        val reviewsDir = configHandler.getReviewsSubDir()
        val sourceDirectory = Path.of(baseDir, exerciseSubDir, reviewsDir)
        val feedbackCount = feedbackFileHelper.readFeedbackCountFromReviews(sourceDirectory.toFile())
        if (feedbackCount.isEmpty()) exitWithError("Reviews folder does not contain any valid files!")

        // Get CSV with count of previous feedbacks
        val feedbackDirPath = configHandler.getFeedbackCsv()
        val feedbackCsv = Path.of(feedbackDirPath).toFile()
        val existingFeedbackCount = try {
            feedbackFileHelper.readFeedbackCountFromCsv(feedbackCsv)
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
            feedbackFileHelper.writeFeedbackCountToCsv(feedbackCsv, mergedCount)
            printlnGreen("Successfully wrote new feedback count to ${feedbackCsv.absolutePath}")
        } catch (e: Exception){
            exitWithError(e.message ?: "Could not write to ${feedbackCsv.absolutePath}")
        }
    }
}
