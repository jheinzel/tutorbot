package at.fhooe.hagenberg.tutorbot.commands

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
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

class SubmissionsCommandTest : CommandLineTest() {
    private val moodleClient = mockk<MoodleClient> {
        every { getHtmlDocument("www.assignment.com") } returns getHtmlResource("websites/Assignment.html")
        every { getHtmlDocument("www.assignment.com/submission") } returns getHtmlResource("websites/Submission.html")
    }
    private val unzipper = Unzipper()
    private val plagiarismChecker = mockk<PlagiarismChecker>()
    private val batchProcessor = BatchProcessor()
    private val configHandler = mockk<ConfigHandler> {
        every { getSubmissionsDownloadLocation() } returns null
    }

    private val submissionsCommand = SubmissionsCommand(moodleClient, unzipper, plagiarismChecker, batchProcessor, configHandler)

    @get:Rule
    val fileSystem = FileSystemRule()

    @Before
    fun setup() {
        val fileSlot = slot<File>() // Download files from resources
        every { moodleClient.downloadFile(any(), capture(fileSlot)) } answers {
            val file = getResource("zip/${fileSlot.captured.name}")
            file.copyTo(fileSlot.captured)
        }
    }

    @Test
    fun `Submissions are downloaded correctly`() {
        systemIn.provideLines(fileSystem.directory.absolutePath, "Yes", "www.assignment.com", "Yes", "Yes", "Yes")

        submissionsCommand.execute()
        verifySubmissions()
    }

    @Test
    fun `Submissions directory is read from config`() {
        every { configHandler.getSubmissionsDownloadLocation() } returns fileSystem.directory.absolutePath
        systemIn.provideLines("Yes", "www.assignment.com", "Yes", "Yes", "Yes")

        submissionsCommand.execute()
        verifySubmissions()
    }

    @Test
    fun `Archives are not deleted if not confirmed`() {
        systemIn.provideLines(fileSystem.directory.absolutePath, "Yes", "www.assignment.com", "No", "Yes", "Yes")

        submissionsCommand.execute()
        verifySubmissions(archiveExists = true)
    }

    @Test
    fun `Plagiarism is not checked if not confirmed`() {
        systemIn.provideLines(fileSystem.directory.absolutePath, "Yes", "www.assignment.com", "Yes", "No", "Yes")

        submissionsCommand.execute()
        confirmVerified(plagiarismChecker)
    }

    @Test
    fun `Submissions are not deleted if not confirmed`() {
        systemIn.provideLines(fileSystem.directory.absolutePath, "Yes", "www.assignment.com", "Yes", "Yes", "No")

        submissionsCommand.execute()
        verifySubmissions(submissionExists = true)
    }

    @Test
    fun `Program exits if the submissions directory is not valid`() {
        systemIn.provideLines(fileSystem.file.absolutePath)
        assertThrows<ProgramExitError> { submissionsCommand.execute() }

        systemIn.provideLines(fileSystem.directory.absolutePath, "No")
        assertThrows<ProgramExitError> { submissionsCommand.execute() }
    }

    private fun verifySubmissions(archiveExists: Boolean = false, submissionExists: Boolean = false) {
        verify { plagiarismChecker.generatePlagiarismReport(fileSystem.directory) }
        assertEquals(archiveExists, File(fileSystem.directory, "submission.zip").exists())
        assertEquals(submissionExists, File(fileSystem.directory, "submission/submission.pdf").exists())
    }
}
