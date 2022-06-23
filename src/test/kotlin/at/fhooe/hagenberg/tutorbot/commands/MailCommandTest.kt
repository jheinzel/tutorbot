package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.auth.CredentialStore
import at.fhooe.hagenberg.tutorbot.components.BatchProcessor
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.network.MailClient
import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.nio.file.Path

class MailCommandTest : CommandLineTest() {
    private val mailClient = mockk<MailClient>()
    private val credentialStore = mockk<CredentialStore> {
        every { getMoodleUsername() } returns "S0"
        every { getEmailPassword() } returns "Password"
    }
    private val configHandler = mockk<ConfigHandler> {
        every { getReviewsDirectoryFromConfig() } returns null
    }
    private fun getReviewsDirectoryFromConfig(): String? {
        return Path.of(configHandler.getBaseDir(), configHandler.getExerciseSubDir(), configHandler.getReviewsSubDir()).toString();
    }
    private val batchProcessor = BatchProcessor()

    private val mailCommand = MailCommand(mailClient, credentialStore, configHandler, batchProcessor)

    @get:Rule
    val fileSystem = FileSystemRule()

    @Before
    fun setup() {
        File(ClassLoader.getSystemResource("pdfs/S1-S2.pdf").toURI()).copyTo(File(fileSystem.directory, "S1-S2.pdf"))
        File(ClassLoader.getSystemResource("pdfs/S3-S4.pdf").toURI()).copyTo(File(fileSystem.directory, "S3-S4.pdf"))
    }

    @Test
    fun `Messages are sent correctly`() {
        systemIn.provideLines(fileSystem.directory.absolutePath, "Subject", "Body", "Yes")

        mailCommand.execute()
        verifySentMails()
    }

    @Test
    fun `Files of wrong type are ignored`() {
        File(ClassLoader.getSystemResource("zip/pdfs.zip").toURI()).copyTo(File(fileSystem.directory, "pdfs.zip"))
        systemIn.provideLines(fileSystem.directory.absolutePath, "Subject", "Body", "Yes")

        mailCommand.execute()
        verifySentMails()
    }

    @Test
    fun `Messages are not sent if not confirmed`() {
        systemIn.provideLines(fileSystem.directory.absolutePath, "Subject", "Body", "No")

        mailCommand.execute()
        confirmVerified(mailClient)
    }

    @Test
    fun `Reviews directory is read from config`() {
        every { getReviewsDirectoryFromConfig() } returns fileSystem.directory.absolutePath
        systemIn.provideLines("Subject", "Body", "Yes")

        mailCommand.execute()
        verifySentMails()
    }

    @Test
    fun `Program exits if the review directory is not valid`() {
        systemIn.provideLines(fileSystem.file.absolutePath)
        assertThrows<ProgramExitError> { mailCommand.execute() }

        systemIn.provideLines(File(fileSystem.directory, "nonexistant").absolutePath)
        assertThrows<ProgramExitError> { mailCommand.execute() }
    }

    private fun verifySentMails() {
        val mail = MailClient.Mail("S0@students.fh-hagenberg.at", emptyList(), "Subject", "Body", fileSystem.file)

        val firstMail = mail.copy(
            to = listOf("S1@students.fh-hagenberg.at", "S2@students.fh-hagenberg.at"),
            attachment = File(fileSystem.directory, "S1-S2.pdf")
        )
        val secondMail = mail.copy(
            to = listOf("S3@students.fh-hagenberg.at", "S4@students.fh-hagenberg.at"),
            attachment = File(fileSystem.directory, "S3-S4.pdf")
        )

        verify { mailClient.sendMail(firstMail) }
        verify { mailClient.sendMail(secondMail) }
        confirmVerified(mailClient)
    }
}
