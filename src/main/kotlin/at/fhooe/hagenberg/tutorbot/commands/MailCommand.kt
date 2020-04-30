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
        val parentDirectory = getParentDirectory()

        // Read sender information from the user
        val subject = promptTextInput("Enter E-Mail subject:")
        val body = promptMultilineTextInput("Enter E-Mail body:")
        val from = getEmail(credentialStore.getUsername())
        credentialStore.getPassword() // Make sure the password is entered by the user

        // Query all PDF files in the directory
        val files = parentDirectory.listFiles { file -> file.extension.toLowerCase() == "pdf" } ?: emptyArray()
        println("Found ${files.size} files")
        if (files.isEmpty()) {
            return // Nothing to do if there are no files
        }

        // Construct all the mail messages
        val mails = files.map { pdf ->
            val (submitter, reviewer) = pdf.nameWithoutExtension.split("-")
            MailClient.Mail(from, listOf(getEmail(submitter), getEmail(reviewer)), subject, body, pdf)
        }

        // Confirm before sending messages -> just to be safe
        if (promptBooleanInput("Do you want to send ${mails.size} emails?")) {
            batchProcessor.process(mails, "Sending emails", "Sent all emails") { mail ->
                mailClient.sendMail(mail)
            }
        }
    }

    private fun getParentDirectory(): File {
        val locationPrompt = "Enter location of the reviewed PDFs (leave empty for current directory):"
        val parentPath = configHandler.getReviewsDownloadLocation() ?: promptTextInput(locationPrompt)
        val parentDirectory = File(parentPath)

        // Make sure the target path points to a directory
        if (!parentDirectory.isDirectory) {
            exitWithError("Location $parentPath does not point to a valid directory.")
        }

        return parentDirectory
    }

    private fun getEmail(username: String): String {
        return "$username@students.fh-hagenberg.at"
    }
}
