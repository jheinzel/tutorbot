package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper.*
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
    
    private companion object {

    }
}
