package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.PlagiarismChecker
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import picocli.CommandLine.Command
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

@Command(
    name = "plagiarism",
    description = ["Checks downloaded submissions for plagiarism."]
)
class PlagiarismCommand @Inject constructor(
    private val plagiarismChecker: PlagiarismChecker,
    private val configHandler: ConfigHandler
) : BaseCommand() {

    override fun execute() {
        val baseDir = configHandler.getBaseDir()
        val exerciseSubDir = configHandler.getExerciseSubDir()
        val submissionsDir = configHandler.getSubmissionsSubDir()
        // Target is exercise dir not submissions dir so it's easier to find the folder
        val targetDirectory = File(baseDir, exerciseSubDir)
        val submissionsDirectory = Path.of(baseDir, exerciseSubDir, submissionsDir).toFile()

        // Make sure the submissions directory exists
        if (!submissionsDirectory.isDirectory) {
            exitWithError("Submissions directory '${submissionsDirectory.absolutePath}' does not point to a valid directory.")
        }

        // Check the results for plagiarism
        plagiarismChecker.generatePlagiarismReport(submissionsDirectory, targetDirectory)
    }
}