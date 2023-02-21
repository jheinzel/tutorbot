package at.fhooe.hagenberg.tutorbot.commands

import picocli.CommandLine.Command
import javax.inject.Inject

@Command(
    name = "instructions",
    description = ["Prints out general instructions on which steps need to be done by the tutor."]
)
class InstructionsCommand @Inject constructor() : BaseCommand() {

    override fun execute() {
        println("To review a homework, the following steps need to be taken:")
        println("- Download all the reviews (use the reviews command). May also download submissions to skip the next step.")
        println("- Download all the submissions and check for plagiarism (use the download submissions command).")
        println("- Check the plagiarism report (index.html) and if there are errors parser.log will have more information.")
        println("- Select reviews randomly by using the choose-feedback command or manually making sure everybody gets chosen fairly.")
        println("- If you have not done it already in the choose-feedback command, save the feedback count using the save-feedback command. This data will be used for choosing reviews next time.")
        println("- Enter which students you are going to review in the excel sheet.")
        println("- Add your feedback to the selected reviews.")
        println("- Collect general feedback and common mistakes for this homework and write it in a markdown file.")
        println("- Send emails to students with the reviewed PDFs (use the mail command).")
        println("- Upload all reviewed PDFs as well as the markdown file to the file share.")
    }
}
