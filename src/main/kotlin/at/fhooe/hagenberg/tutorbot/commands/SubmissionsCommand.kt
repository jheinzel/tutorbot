package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.auth.MoodleAuthenticator
import at.fhooe.hagenberg.tutorbot.components.BatchProcessor
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.PlagiarismChecker
import at.fhooe.hagenberg.tutorbot.components.Unzipper
import at.fhooe.hagenberg.tutorbot.network.MoodleClient
import at.fhooe.hagenberg.tutorbot.util.href
import at.fhooe.hagenberg.tutorbot.util.printlnRed
import at.fhooe.hagenberg.tutorbot.util.promptBooleanInput
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import picocli.CommandLine.Command
import java.io.File
import java.net.URI
import javax.inject.Inject
import kotlin.system.exitProcess

@Command(
    name = "submissions",
    description = ["Downloads all submissions for a certain exercise"]
)
class SubmissionsCommand @Inject constructor(
    private val moodleClient: MoodleClient,
    private val unzipper: Unzipper,
    private val plagiarismChecker: PlagiarismChecker,
    private val batchProcessor: BatchProcessor,
    private val configHandler: ConfigHandler,
    private val authenticator: MoodleAuthenticator
) : DownloadCommand(configHandler) {

    override fun execute() {
        execute(null, null)
    }

    fun execute(assignmentUrl: String?, submissionOptions: Triple<Boolean, Boolean, Boolean>?) {
        authenticator.authenticate()

        val submissionsDirectory = setupTargetDirectory()
        val url = assignmentUrl ?: promptTextInput("Enter assignment URL:")
        val (deleteArchives, checkPlagiarism, deleteSubmissions) = submissionOptions ?: promptForSubmissionOptions()

        var downloadLinks = getAllDownloadLinks(url)
        while (downloadLinks.isEmpty()) {
            printlnRed("Could not find any submissions to download")
            if (!promptBooleanInput("Try again? (Y/N)")) {
                exitProcess(0)
            }
            downloadLinks = getAllDownloadLinks(promptTextInput("Enter assignment URL:"))
        }
        val files = downloadLinks.map { link -> File(submissionsDirectory, getFileName(link)) }

        // Download and unzip all submitted solutions
        val submissions = downloadLinks.zip(files)
        batchProcessor.process(submissions, "Downloading submissions", "Download completed") { (link, file) ->
            moodleClient.downloadFile(link, file)
            unzipper.unzipFile(file) // Extract the archive
        }

        // Delete all archives if wanted
        if (deleteArchives) {
            files.forEach { archive -> archive.delete() }
        }

        // Check the results for plagiarism if wanted
        if (checkPlagiarism) {
            val baseDir = configHandler.getBaseDir()
            val exerciseSubDir = configHandler.getExerciseSubDir()
            val targetDirectory = File(baseDir, exerciseSubDir)
            plagiarismChecker.generatePlagiarismReport(submissionsDirectory, targetDirectory)
        }

        // Delete the downloaded submissions if wanted
        if (deleteSubmissions) {
            files.map { zip -> File(zip.parent, zip.nameWithoutExtension) }.forEach { file ->
                file.deleteRecursively()
            }
        }
    }

    override fun getCommandSubDir(): String {
        return configHandler.getSubmissionsSubDir()
    }

    private fun getAllDownloadLinks(assignmentUrl: String): List<String> = try {
        val assignmentPage =
            moodleClient.getHtmlDocument(assignmentUrl) // Query the links to all submission detail pages
        val detailUrls = assignmentPage.select(".submission a.title").map { element -> element.href() }

        // Follow the links to all detail pages and extract the real download URL
        batchProcessor.process(detailUrls, "Gathering download URLs", "Gathered download URLs") { url ->
            val detailPage = moodleClient.getHtmlDocument(url)
            detailPage.selectFirst(".submission-full .files a").href()
        }
    } catch (exception: Exception) {
        emptyList() // Download URLs could not be parsed
    }

    private fun getFileName(link: String): String {
        return URI(link).path.split("/").last()
    }


}

fun promptForSubmissionOptions(): Triple<Boolean, Boolean, Boolean> {
    val deleteArchives = promptBooleanInput("Do you want to delete the extracted archives?")
    val checkPlagiarism = promptBooleanInput("Do you want to check submissions for plagiarism?")
    val deleteSubmissions = promptBooleanInput("Do you want to delete the downloaded submissions again?")
    return Triple(deleteArchives, checkPlagiarism, deleteSubmissions)
}

