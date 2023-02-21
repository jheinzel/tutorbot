package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.auth.MoodleAuthenticator
import at.fhooe.hagenberg.tutorbot.components.BatchProcessor
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.network.MoodleClient
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.href
import at.fhooe.hagenberg.tutorbot.util.promptBooleanInput
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import picocli.CommandLine.Command
import java.io.File
import javax.inject.Inject

@Command(
    name = "reviews",
    description = ["Downloads all reviews for a certain exercise. Optionally also downloads the submissions and performs a plagiarism check."]
)
class ReviewsCommand @Inject constructor(
    private val moodleClient: MoodleClient,
    private val batchProcessor: BatchProcessor,
    private val configHandler: ConfigHandler,
    private val authenticator: MoodleAuthenticator,
    private val submissionsCommand: SubmissionsCommand
) : DownloadCommand(configHandler) {

    override fun execute() {
        authenticator.authenticate()
        val targetDirectory = setupTargetDirectory()

        val assignmentUrl = promptTextInput("Enter assignment URL:")
        val detailUrls = getAllDetailLinks(assignmentUrl)

        var submissionOptions: Triple<Boolean, Boolean, Boolean>? = null
        if (promptBooleanInput("Also download submissions?")) {
            submissionOptions = promptForSubmissionOptions()
        }

        // Follow the detail links and extract the real download URL as well as the file name
        val reviews = batchProcessor.process(
            detailUrls,
            "Gathering review download URLs",
            "Gathered review download URLs"
        ) { url ->
            val detailPage = moodleClient.getHtmlDocument(url)

            // Extract the student number of the submitter
            val submitterTag = detailPage.selectFirst(".submission-full .fullname a")
            val submitterProfileUrl = submitterTag.href()
            val submitterFullName = submitterTag.text().replace(" ", "")
            val submitter = getStudentNumber(submitterProfileUrl)

            // Extract the student number of the reviewer
            val reviewerTag = detailPage.selectFirst(".assessment-full .fullname a")
            val reviewerProfileUrl = reviewerTag.href()
            val reviewerFullName = reviewerTag.text().replace(" ", "")
            val reviewer = getStudentNumber(reviewerProfileUrl)

            // Extract the download link and return it in combination with the target file
            val downloadLink = detailPage.selectFirst(".overallfeedback .files a")?.href()
            if (submitter != null && reviewer != null && downloadLink != null) {
                downloadLink to File(targetDirectory, "$submitter-${reviewer}_$submitterFullName-$reviewerFullName.pdf")
            } else {
                null // Could not parse all necessary elements -> skip entry
            }
        }
        if (reviews.isEmpty()) {
            exitWithError("Could not find any reviews to download")
        }

        // Download all submitted reviews
        batchProcessor.process(reviews, "Downloading reviews", "Download completed") { (link, file) ->
            moodleClient.downloadFile(link, file)
        }

        if (submissionOptions != null) {
            submissionsCommand.execute(assignmentUrl, submissionOptions)
        }
    }

    override fun getCommandSubDir(): String {
        return configHandler.getReviewsSubDir()
    }

    private fun getAllDetailLinks(assignmentUrl: String): List<String> = try {
        val assignmentPage = moodleClient.getHtmlDocument(assignmentUrl)
        assignmentPage.select(".receivedgrade a.grade").map { element -> element.href() }
    } catch (exception: Exception) {
        emptyList() // Detail URLs could not be parsed
    }

    private fun getStudentNumber(profileUrl: String): String? = try {
        val profilePage = moodleClient.getHtmlDocument(profileUrl)
        val email = profilePage.selectFirst("a[href^='mailto:']").text()
        email.substringBefore("@")
    } catch (exception: Exception) {
        null // Student number could not be extracted
    }
}
