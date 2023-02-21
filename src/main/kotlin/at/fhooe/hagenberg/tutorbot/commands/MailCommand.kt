package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.auth.CredentialStore
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.network.MailClient
import at.fhooe.hagenberg.tutorbot.util.*
import picocli.CommandLine.Command
import java.io.File
import java.nio.file.Path
import javax.inject.Inject
import javax.mail.AuthenticationFailedException

@Command(
    name = "mail",
    description = ["Sends PDFs containing the feedback via email."]
)
class MailCommand @Inject constructor(
    private val mailClient: MailClient,
    private val credentialStore: CredentialStore,
    private val configHandler: ConfigHandler
) : BaseCommand() {

    override fun execute() {
        val reviewsDirectory = getReviewsDirectory()

        // Query all PDF files in the directory
        val files = reviewsDirectory.listFiles { file -> file.extension.toLowerCase() == "pdf" } ?: emptyArray()
        println("Found ${files.size} files")
        if (files.isEmpty()) {
            return // Nothing to do if there are no files
        }

        // Read sender information from the user
        val subject = configHandler.getEmailSubjectTemplate()?.promptTemplateArguments("Subject")
            ?: promptTextInput("Enter email subject:")
        val body = configHandler.getEmailBodyTemplate()?.promptTemplateArguments("Body")
            ?: promptMultilineTextInput("Enter email body:")
        val from = credentialStore.getEmailAddress()

        // Make sure the password is entered by the user and the password works
        while (true) {
            try {
                print("Trying to send Testmail to: $from ... ")
                mailClient.sendMail(MailClient.Mail(from, listOf(from), subject, body, files[0]))
                printlnGreen("success!")
                break
            } catch (authEx: AuthenticationFailedException) {
                printlnRed("Password is not correct, try again")
                credentialStore.setEmailPassword(promptPasswordInput("Enter email password:"))
            } catch (exception: Exception){
                exitWithError("${exception.message}")
            }
        }

        // Construct all the mail messages
        val mails = files.map { pdf ->
            val (submitter, reviewer) = pdf.nameWithoutExtension.split("_")[0].split("-")
            MailClient.Mail(from, listOf(getStudentEmail(submitter), getStudentEmail(reviewer)), subject, body, pdf)
        }

        // Confirm before sending messages -> just to be safe
        if (promptBooleanInput("Do you want to send ${mails.size} emails?")) {
            println("Sending Emails to:")
            mails.mapIndexedNotNull { index, mail ->
                try {
                    print("\t(${index + 1}/${mails.size}) ${mail.to} ... ")
                    mailClient.sendMail(mail)
                    printlnGreen("success")
                } catch (exception: Exception) {
                    printlnRed("failed (${exception::class.java.typeName}; ${exception.message})")
                }
            }
        }
    }

    private fun String.promptTemplateArguments(name: String): String {
        println("$name template found:")
        printlnGreen(this)

        while (true) {
            val args = promptTextInput("Supply template arguments (separate args with ;):")
                .split(";").map { arg -> arg.trim() }.toTypedArray()
            try {
                val formatted = this.format(*args)
                println("Formatted:")
                printlnCyan(formatted)
                if (promptBooleanInput("$name formatted correctly?"))
                    return formatted
            } catch (exception: Exception) {
                printlnRed("Could not format string, try again (${exception.message})")
            }
        }
    }

    private fun getReviewsDirectory(): File {
        val baseDir = configHandler.getBaseDir()
        val exerciseSubDir = configHandler.getExerciseSubDir()
        val reviewsSubDir = configHandler.getReviewsSubDir()

        val reviewsDirectory = File(Path.of(baseDir, exerciseSubDir, reviewsSubDir).toString())

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
