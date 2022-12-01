package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.util.promptNumberInput
import picocli.CommandLine.Command
import javax.inject.Inject
import kotlin.random.Random

@Command(
    name = "choose-feedback",
    description = ["Choose reviews to give feedback on. Students who have gotten less feedbacks on submissions or reviews have a greater chance of being picked. To avoid predictability, randomness is also added."]
)
class ChooseFeedbackCommand @Inject constructor(
    private val configHandler: ConfigHandler,
    private val random: Random
) : BaseCommand() {
    // Tracks amount of feedbacks a student has received on submissions and reviews.
    private data class FeedbackCount(val studentNr: String, val submission: Int, val review: Int)
    // Represents a Review, which has a student who submitted the code and one who reviewed it.
    private data class Review(val fileName: String, val studentSubNr: String, val studentRevNr: String) // TODO calculate sub and rev from fileName

    /**
     * Choose students who have gotten the least feedbacks on submissions, preferring them if they also got less on reviews.
     * If they have the same amount of reviews in both categories, they are chosen randomly.
     * Additionally random students submissions will be picked regardless of feedback count if enabled.
     */
    override fun execute() {
        val feedbackCount =
            configHandler.getFeedbackAmount() ?: promptNumberInput("Enter amount of reviews to pick:")
        val randomCount =
            configHandler.getFeedbackRandomAmount() ?: promptNumberInput("Enter amount of reviews to pick:")
        TODO("Implement logic described above")
    }

    private fun readFeedbackCountFromDir(): Collection<FeedbackCount> = TODO("Read files from basedir/feedback.dir, sum up occurrences on left side as submitter, right side reviewer.")

    private fun readAvailableReviewsFromDir(): Collection<Review> = TODO("Read filenames from basedir/ex../review.dir")

    private companion object {

    }
}
