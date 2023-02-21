package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.auth.MoodleAuthenticator
import at.fhooe.hagenberg.tutorbot.components.BatchProcessor
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.PlagiarismChecker
import at.fhooe.hagenberg.tutorbot.components.Unzipper
import at.fhooe.hagenberg.tutorbot.network.MoodleClient
import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.getHtmlResource
import at.fhooe.hagenberg.tutorbot.testutil.getResource
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.nio.file.Path

class SubmissionsCommandTest : CommandLineTest() {
    private val testFileName = "S1-S2_S1-S2.pdf"
    private val submissionsLoc = "submissions"
    private val exerciseLoc = "ue01"

    private val moodleClient = mockk<MoodleClient> {
        every { getHtmlDocument("www.assignment.com") } returns getHtmlResource("websites/Assignment.html")
        every { getHtmlDocument("www.assignment.com/submission") } returns getHtmlResource("websites/Submission.html")
    }
    private val unzipper = Unzipper()
    private val plagiarismChecker = mockk<PlagiarismChecker>()
    private val batchProcessor = BatchProcessor()
    private val configHandler = mockk<ConfigHandler>() {
        every { getExerciseSubDir() } returns exerciseLoc
        every { getSubmissionsSubDir() } returns submissionsLoc
    }
    private val moodleAuthenticator = mockk<MoodleAuthenticator>()
    private val submissionsCommand = SubmissionsCommand(
        moodleClient,
        unzipper,
        plagiarismChecker,
        batchProcessor,
        configHandler,
        moodleAuthenticator
    )

    @get:Rule
    val fileSystem = FileSystemRule()

    @Before
    fun setup() {
        every { configHandler.getBaseDir() } returns fileSystem.directory.absolutePath.toString()

        val fileSlot = slot<File>() // Download files from resources
        every { moodleClient.downloadFile(any(), capture(fileSlot)) } answers {
            val file = getResource("zip/${fileSlot.captured.name}")
            file.copyTo(fileSlot.captured)
        }
    }

    @Test
    fun `Submissions are downloaded correctly`() {
        systemIn.provideLines("www.assignment.com", "Y", "Y", "Y")

        submissionsCommand.execute()
        verifySubmissions()
    }

    @Test
    fun `Archives are not deleted if not confirmed`() {
        systemIn.provideLines("www.assignment.com", "N", "Y", "Y")

        submissionsCommand.execute()
        verifySubmissions(archiveExists = true)
    }

    @Test
    fun `Plagiarism is not checked if not confirmed`() {
        systemIn.provideLines("www.assignment.com", "Y", "N", "Y")

        submissionsCommand.execute()
        confirmVerified(plagiarismChecker)
    }

    @Test
    fun `Submissions are not deleted if not confirmed`() {
        systemIn.provideLines("www.assignment.com", "Y", "Y", "N")

        submissionsCommand.execute()
        verifySubmissions(submissionExists = true)
    }

    private fun verifySubmissions(archiveExists: Boolean = false, submissionExists: Boolean = false) {
        val submissionsLoc = Path.of(fileSystem.directory.absolutePath, exerciseLoc, submissionsLoc).toFile()
        val outputLoc = Path.of(fileSystem.directory.absolutePath, exerciseLoc).toFile()
        verify { plagiarismChecker.generatePlagiarismReport(submissionsLoc, outputLoc) }
        assertEquals(archiveExists, File(submissionsLoc, "submission.zip").exists())
        assertEquals(submissionExists, File(submissionsLoc, "submission/submission.pdf").exists())
    }
}
