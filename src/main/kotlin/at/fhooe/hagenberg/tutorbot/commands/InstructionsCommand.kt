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
        println("- Download all the reviews (use the reviews command). May also download submissions in this step if needed.")
        println("- Download all the submissions and check for plagiarism (use the download submissions command).")
        println("- Select reviews randomly by using the choose-feedback command or manually making sure everybody gets chosen fairly.")
        println("- Add your feedback to the selected reviews.")
        println("- Collect general feedback and common mistakes for this homework and write it in a markdown file.")
        println("- Send emails to students with the reviewed PDFs (use the mail command). May also save the feedback counts in this step.")
        println("- (Do this only if it was not already done in the mail command!) Save the feedback amount using the save-feedback command. This data will be used for choosing reviews next time.")
        println("- Upload all reviewed PDfs as well as the markdown file to the file share.")
        println("- Enter which students you reviewed in the excel sheet.")
    }
}
