package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.auth.MoodleAuthenticator
import at.fhooe.hagenberg.tutorbot.components.BatchProcessor
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.PlagiarismChecker
import at.fhooe.hagenberg.tutorbot.components.Unzipper
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
import java.nio.file.Path

class ReviewsCommandTest : CommandLineTest() {
    private val testFileName = "S1-S2_S1-S2.pdf"
    private val reviewLoc = "reviews"
    private val exerciseLoc = "ue01"

    private val moodleClient = mockk<MoodleClient> {
        every { getHtmlDocument("www.assignment.com") } returns getHtmlResource("websites/Assignment.html")
        every { getHtmlDocument("www.assignment.com/details") } returns getHtmlResource("websites/Details.html")
        every { getHtmlDocument("www.assignment.com/S1") } returns getHtmlResource("websites/S1.html")
        every { getHtmlDocument("www.assignment.com/S2") } returns getHtmlResource("websites/S2.html")
    }
    private val configHandler = mockk<ConfigHandler> {
        every { getExerciseSubDir() } returns exerciseLoc
        every { getReviewsSubDir() } returns reviewLoc
    }
    private val batchProcessor = BatchProcessor()
    private val moodleAuthenticator = mockk<MoodleAuthenticator>()
    private val unzipper = Unzipper()
    private val plagiarismChecker = mockk<PlagiarismChecker>()
    private val submissionsCommand = SubmissionsCommand(
        moodleClient,
        unzipper,
        plagiarismChecker,
        batchProcessor,
        configHandler,
        moodleAuthenticator
    )
    private val reviewsCommand =
        ReviewsCommand(moodleClient, batchProcessor, configHandler, moodleAuthenticator, submissionsCommand)

    @get:Rule
    val fileSystem = FileSystemRule()

    @Before
    fun setup() {
        every { configHandler.getBaseDir() } returns fileSystem.directory.absolutePath.toString()

        val fileSlot = slot<File>() // Download files from resources
        every { moodleClient.downloadFile(any(), capture(fileSlot)) } answers {
            val file = getResource("pdfs/${fileSlot.captured.name}")
            file.copyTo(fileSlot.captured)
        }
    }

    @Test
    fun `Reviews are downloaded correctly with properties`() {
        systemIn.provideLines("www.assignment.com", "N")

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
    fun `Program exits if the input directory is not valid`() {
        val exLoc =
            Path.of(fileSystem.directory.absolutePath, exerciseLoc)
                .toFile()
        exLoc.mkdirs()
        val reviewFile = Path.of(exLoc.toString(), reviewLoc).toFile()
        reviewFile.createNewFile()
        // Does not allow when directory == file
        assertThrows<ProgramExitError> { reviewsCommand.execute() }
        reviewFile.delete()
        reviewFile.mkdirs()
        // Does not allow when exists and override is N
        systemIn.provideLines(fileSystem.directory.absolutePath, "N")
        assertThrows<ProgramExitError> { reviewsCommand.execute() }
    }

    private fun verifyReviews() {
        val reviewLoc =
            Path.of(fileSystem.directory.absolutePath, exerciseLoc, reviewLoc)
                .toString()
        assertTrue(File(reviewLoc, testFileName).exists())
    }
}
