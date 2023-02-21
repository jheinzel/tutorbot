package at.fhooe.hagenberg.tutorbot.commands

import picocli.CommandLine.Command
import javax.inject.Inject

@Command(
    name = "version",
    description = ["Shows tutorbot version information."]
)
class VersionCommand @Inject constructor() : BaseCommand() {

    override fun execute() {
        println("Tutorbot version: ${javaClass.`package`.implementationVersion}")
        println("You can find the latest version as well as changelog information here:")
        println("https://github.com/jheinzel/tutorbot/releases/")
    }
}
