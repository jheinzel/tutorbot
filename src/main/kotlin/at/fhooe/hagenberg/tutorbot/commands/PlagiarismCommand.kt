package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.PlagiarismChecker
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import picocli.CommandLine.Command
import java.io.File
import javax.inject.Inject

@Command(
    name = "plagiarism",
    description = ["Checks downloaded submissions for plagiarism"]
)
class PlagiarismCommand @Inject constructor(
    private val plagiarismChecker: PlagiarismChecker
) : BaseCommand() {

    override fun execute() {
        val submissionsPath = promptTextInput("Enter submissions location (leave empty for current directory):")
        val submissionsDirectory = File(submissionsPath)

        // Make sure the submissions directory exists
        if (!submissionsDirectory.isDirectory) {
            exitWithError("Submissions directory $submissionsPath does not point to a valid directory.")
        }

        // Check the results for plagiarism
        plagiarismChecker.generatePlagiarismReport(submissionsDirectory)
    }
}