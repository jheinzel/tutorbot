package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.BatchProcessor
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.network.MoodleClient
import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import at.fhooe.hagenberg.tutorbot.testutil.getHtmlResource
import at.fhooe.hagenberg.tutorbot.testutil.getResource
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class ReviewsCommandTest : CommandLineTest() {
    private val moodleClient = mockk<MoodleClient> {
        every { getHtmlDocument("www.assignment.com") } returns getHtmlResource("websites/Assignment.html")
        every { getHtmlDocument("www.assignment.com/details") } returns getHtmlResource("websites/Details.html")
        every { getHtmlDocument("www.assignment.com/S1") } returns getHtmlResource("websites/S1.html")
        every { getHtmlDocument("www.assignment.com/S2") } returns getHtmlResource("websites/S2.html")
    }
    private val batchProcessor = BatchProcessor()
    private val configHandler = mockk<ConfigHandler> {
        every { getReviewsDownloadLocation() } returns null
    }

    private val reviewsCommand = ReviewsCommand(moodleClient, batchProcessor, configHandler)

    @get:Rule
    val fileSystem = FileSystemRule()

    @Before
    fun setup() {
        val fileSlot = slot<File>() // Download files from resources
        every { moodleClient.downloadFile(any(), capture(fileSlot)) } answers {
            val file = getResource("pdfs/${fileSlot.captured.name}")
            file.copyTo(fileSlot.captured)
        }
    }

    @Test
    fun `Reviews are downloaded correctly`() {
        systemIn.provideLines(fileSystem.directory.absolutePath, "Yes", "www.assignment.com")

        reviewsCommand.execute()
        verifyReviews()
    }

    @Test
    fun `Reviews directory is read from config`() {
        every { configHandler.getReviewsDownloadLocation() } returns fileSystem.directory.absolutePath
        systemIn.provideLines("Yes", "www.assignment.com")

        reviewsCommand.execute()
        verifyReviews()
    }

    @Test
    fun `Program exits if no reviews are found`() {
        every { moodleClient.getHtmlDocument("www.assignment.com") } returns getHtmlResource("websites/Blank.html")
        systemIn.provideLines(fileSystem.directory.absolutePath, "Yes", "www.assignment.com")

        assertThrows<ProgramExitError> { reviewsCommand.execute() }
    }

    @Test
    fun `Program exits if the reviews directory is not valid`() {
        systemIn.provideLines(fileSystem.file.absolutePath)
        assertThrows<ProgramExitError> { reviewsCommand.execute() }

        systemIn.provideLines(fileSystem.directory.absolutePath, "No")
        assertThrows<ProgramExitError> { reviewsCommand.execute() }
    }

    private fun verifyReviews() {
        assertTrue(File(fileSystem.directory, "S1-S2.pdf").exists())
    }
}
