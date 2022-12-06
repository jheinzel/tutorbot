package at.fhooe.hagenberg.tutorbot

import at.fhooe.hagenberg.tutorbot.commands.*
import at.fhooe.hagenberg.tutorbot.di.DaggerTutorbotComponent
import picocli.CommandLine
import picocli.CommandLine.Command
import kotlin.system.exitProcess

@Command(
    name = "tutorbot",
    subcommands = [
        SubmissionsCommand::class,
        ReviewsCommand::class,
        MailCommand::class,
        PlagiarismCommand::class,
        ChooseFeedbackCommand::class,
        SaveFeedbackCommand::class,
        InstructionsCommand::class,
        VersionCommand::class
    ]
)
object Tutorbot : Runnable {
    private val commandLine by lazy {
        val component = DaggerTutorbotComponent.create()
        CommandLine(this, component.commandFactory())
    }

    @JvmStatic
    fun main(args: Array<String>) {
        exitProcess(commandLine.execute(*args)) // Bootstrap
    }

    override fun run() {
        commandLine.usage(System.out)
    }
}
