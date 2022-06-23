package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.auth.CredentialStore
import at.fhooe.hagenberg.tutorbot.components.BatchProcessor
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.network.MailClient
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.promptBooleanInput
import at.fhooe.hagenberg.tutorbot.util.promptMultilineTextInput
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import picocli.CommandLine.Command
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

@Command(
    name = "mail",
    description = ["Sends PDFs containing the feedback via email"]
)
class MailCommand @Inject constructor(
    private val mailClient: MailClient,
    private val credentialStore: CredentialStore,
    private val configHandler: ConfigHandler,
    private val batchProcessor: BatchProcessor
) : BaseCommand() {

    override fun execute() {
        val reviewsDirectory = getReviewsDirectory()

        // Read sender information from the user
        val subject = promptTextInput("Enter email subject:")
        val body = promptMultilineTextInput("Enter email body:")
        val from = credentialStore.getEmailAddress()
        credentialStore.getEmailPassword() // Make sure the password is entered by the user

        // Query all PDF files in the directory
        val files = reviewsDirectory.listFiles { file -> file.extension.toLowerCase() == "pdf" } ?: emptyArray()
        println("Found ${files.size} files")
        if (files.isEmpty()) {
            return // Nothing to do if there are no files
        }

        // Construct all the mail messages
        val mails = files.map { pdf ->
            val (submitter, reviewer) = pdf.nameWithoutExtension.split("-")
            MailClient.Mail(from, listOf(getStudentEmail(submitter), getStudentEmail(reviewer)), subject, body, pdf)
        }

        // Confirm before sending messages -> just to be safe
        if (promptBooleanInput("Do you want to send ${mails.size} emails?")) {
            batchProcessor.process(mails, "Sending emails", "Sent all emails") { mail ->
                mailClient.sendMail(mail)
            }
        }
    }


    private fun getReviewsDirectory(): File {
        val baseDir        = configHandler.getBaseDir()        ?: promptTextInput("Enter base directory:")
        val exerciseSubDir = configHandler.getExerciseSubDir() ?: promptTextInput("Enter exercise subdirectory:")
        val reviewsSubDir  = configHandler.getReviewsSubDir()  ?: promptTextInput("Enter reviews subdirectory:")

        var reviewsDirectory = File(Path.of(baseDir, exerciseSubDir, reviewsSubDir).toString());

        // Make sure the target path points to a directory
        if (!reviewsDirectory.isDirectory) {
            exitWithError("Location $reviewsDirectory does not point to a valid directory.")
        }

        return reviewsDirectory
    }

    private fun getStudentEmail(username: String): String {
         return "$username@${configHandler.getStudentsEmailSuffix()}"
    }
}
