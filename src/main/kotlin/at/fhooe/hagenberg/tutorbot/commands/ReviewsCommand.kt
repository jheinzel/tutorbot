package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.BatchProcessor
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.network.MoodleClient
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.href
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import picocli.CommandLine.Command
import java.io.File
import javax.inject.Inject

@Command(
    name = "reviews",
    description = ["Downloads all reviews for a certain exercise"]
)
class ReviewsCommand @Inject constructor(
    private val moodleClient: MoodleClient,
    private val batchProcessor: BatchProcessor,
    private val configHandler: ConfigHandler
) : DownloadCommand() {

    override fun execute() {
        val targetDirectory = setupTargetDirectory()

        val assignmentUrl = promptTextInput("Enter assignment URL:")
        val detailUrls = getAllDetailLinks(assignmentUrl)

        // Follow the detail links and extract the real download URL as well as the file name
        val reviews = batchProcessor.process(detailUrls,  "Gathering download URLs", "Gathered download URLs") { url ->
            val detailPage = moodleClient.getHtmlDocument(url)

            // Extract the student number of the submitter
            val submitterProfileUrl = detailPage.selectFirst(".submission-full .fullname a").href()
            val submitter = getStudentNumber(submitterProfileUrl)

            // Extract the student number of the reviewer
            val reviewerProfileUrl = detailPage.selectFirst(".assessment-full .fullname a").href()
            val reviewer = getStudentNumber(reviewerProfileUrl)

            // Extract the download link and return it in combination with the target file
            val downloadLink = detailPage.selectFirst(".overallfeedback .files a")?.href()
            if (submitter != null && reviewer != null && downloadLink != null) {
                downloadLink to File(targetDirectory, "$submitter-$reviewer.pdf")
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
    }

    override fun getTargetDirectoryFromConfig(): String? {
        return configHandler.getReviewsDownloadLocation()
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
