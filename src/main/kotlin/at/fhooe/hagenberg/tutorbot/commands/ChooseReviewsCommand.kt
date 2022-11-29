package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.promptBooleanInput
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import java.io.File
import java.nio.file.Path
import picocli.CommandLine.Command
import javax.inject.Inject

@Command(
        name = "choose-reviews",
        description = ["Choose an amount of reviews to work on. Students who have gotten less feedbacks on submissions or reviews have a greater chance of being picked."]
)
class ChooseReviewsCommand @Inject constructor(
        private val configHandler: ConfigHandler
) : BaseCommand() {
        override fun execute() {

        }
}
