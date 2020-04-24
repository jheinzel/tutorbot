package at.fhooe.hagenberg.tutorbot.commands

import picocli.CommandLine.Command
import javax.inject.Inject

@Command(
    name = "instructions",
    description = ["Prints out general instructions on which steps need to be done by the tutor"]
)
class InstructionsCommand @Inject constructor() : BaseCommand() {

    override fun execute() {
        println("To review a homework, the following steps need to be taken:")
        println("- Download all the submissions and check for plagiarism (use the download submissions command)")
        println("- Download all the reviews (use the reviews command)")
        println("- Select reviews randomly (make sure everybody gets chosen once) and add your feedback")
        println("- Collect general feedback and common mistakes for this homework and collect it in a markdown file")
        println("- Send emails to students with the reviewed PDFs (use the mail command)")
        println("- Upload all reviewed PDfs as well as the markdown file to the file share")
        println("- Enter which students you reviewed in the excel sheet")
    }
}
