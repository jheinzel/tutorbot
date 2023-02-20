package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.auth.CredentialStore
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
import java.io.Console
import java.io.File
import java.nio.file.Path
import javax.mail.AuthenticationFailedException

class MailCommandTest : CommandLineTest() {
    private val mailClient = mockk<MailClient>()

    private val saveFeedbackCommand = mockk<SaveFeedbackCommand>()

    private val credentialStore = mockk<CredentialStore> {
        every { getEmailAddress() } returns "S0@example.org"
        every { getEmailUsername() } returns "S0"
        every { getEmailPassword() } returns "Password"
    }

    private val configHandler = mockk<ConfigHandler>() {
        every { getStudentsEmailSuffix() } returns "example.org"
        every { getExerciseSubDir() } returns "ue01"
        every { getReviewsSubDir() } returns "reviews"
    }

    private val mailCommand = MailCommand(mailClient, credentialStore, configHandler)

    @get:Rule
    val fileSystem = FileSystemRule()

    private lateinit var file1: File
    private lateinit var file2: File

    @Before
    fun setup() {
        // Setup example files at the temporary fileSystem location
        every { configHandler.getBaseDir() } returns fileSystem.directory.absolutePath.toString()
        // No template in default setup
        every { configHandler.getEmailSubjectTemplate() } returns null
        every { configHandler.getEmailBodyTemplate() } returns null

        val reviewLoc =
            Path.of(configHandler.getBaseDir(), configHandler.getExerciseSubDir(), configHandler.getReviewsSubDir())
                .toString()
        file1 = File(ClassLoader.getSystemResource("pdfs/S1-S2_S1-S2.pdf").toURI()).copyTo(File(reviewLoc, "S1-S2_S1-S2.pdf"))
        file2 = File(ClassLoader.getSystemResource("pdfs/S3-S4_S3-S4.pdf").toURI()).copyTo(File(reviewLoc, "S3-S4_S3-S4.pdf"))
    }

    @Test
    fun `Test mail is sent to sender`() {
        systemIn.provideLines("Subject", "Body", "Y")

        mailCommand.execute()
        verifyTestMail()
    }

    @Test
    fun `Template is used when present`() {
        every { configHandler.getEmailSubjectTemplate() } returns "Subject %s"
        every { configHandler.getEmailBodyTemplate() } returns "Body %s"
        systemIn.provideLines("1", "Y", "1", "Y", "Y")

        mailCommand.execute()
        verifySentMails("Subject 1", "Body 1")
    }

    @Test
    fun `Mails are sent to both recipients`() {
        systemIn.provideLines("Subject", "Body", "Y")

        mailCommand.execute()
        verifySentMails()
    }

    @Test
    fun `Failed authorization allows retry`() {
        val console = mockk<Console>()
        systemIn.provideLines("Subject", "Body", "Y")
        mockkStatic(System::class)
        every { System.console() } returns console
        every { console.readPassword() } returns "password".toCharArray()
        every {mailClient.sendMail(any()) } throws AuthenticationFailedException() andThen Unit

        mailCommand.execute()
        verifySentMails()
    }

    @Test
    fun `Files of wrong type are ignored`() {
        File(ClassLoader.getSystemResource("zip/pdfs.zip").toURI()).copyTo(File(fileSystem.directory, "pdfs.zip"))
        systemIn.provideLines("Subject", "Body", "Y")

        mailCommand.execute()
        verifySentMails()
    }

    @Test
    fun `Messages are not sent if not confirmed`() {
        systemIn.provideLines("Subject", "Body", "N")

        mailCommand.execute()
        verifyTestMail()
        confirmVerified(mailClient)
    }

    @Test
    fun `Program exits if the base directory is not valid`() {
        every { configHandler.getBaseDir() } returns File(fileSystem.directory, "nonexistant").absolutePath

        assertThrows<ProgramExitError> { mailCommand.execute() }
    }

    private fun verifyTestMail(){
        verify { mailClient.sendMail(any()) }
    }

    private fun verifySentMails(expectedSubject: String = "Subject", expectedBody: String = "Body") {
        verifyTestMail()

        val emailSuffix = configHandler.getStudentsEmailSuffix()
        val mail = MailClient.Mail("S0@$emailSuffix", listOf("S0@$emailSuffix"), expectedSubject, expectedBody, fileSystem.file)

        val firstMail = mail.copy(
            to = listOf("S1@$emailSuffix", "S2@$emailSuffix"),
            attachment = file1
        )
        val secondMail = mail.copy(
            to = listOf("S3@$emailSuffix", "S4@$emailSuffix"),
            attachment = file2
        )

        verify { mailClient.sendMail(firstMail) }
        verify { mailClient.sendMail(secondMail) }
        confirmVerified(mailClient)
    }
}
