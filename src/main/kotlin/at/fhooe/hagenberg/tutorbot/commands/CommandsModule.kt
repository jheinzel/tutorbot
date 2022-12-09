package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.di.CommandKey
import at.fhooe.hagenberg.tutorbot.di.DaggerFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import picocli.CommandLine

@Module
interface CommandsModule {

    @Binds
    fun bindFactory(factory: DaggerFactory): CommandLine.IFactory

    @Binds
    @IntoMap
    @CommandKey(InstructionsCommand::class)
    fun bindInstructionsCommand(command: InstructionsCommand): BaseCommand

    @Binds
    @IntoMap
    @CommandKey(MailCommand::class)
    fun bindMailCommand(command: MailCommand): BaseCommand

    @Binds
    @IntoMap
    @CommandKey(PlagiarismCommand::class)
    fun bindPlagiarismCommand(command: PlagiarismCommand): BaseCommand

    @Binds
    @IntoMap
    @CommandKey(ReviewsCommand::class)
    fun bindReviewsCommand(command: ReviewsCommand): BaseCommand

    @Binds
    @IntoMap
    @CommandKey(ChooseFeedbackCommand::class)
    fun bindChooseFeedbackCommand(command: ChooseFeedbackCommand): BaseCommand

    @Binds
    @IntoMap
    @CommandKey(SaveFeedbackCommand::class)
    fun bindSaveFeedbackCommand(command: SaveFeedbackCommand): BaseCommand

    @Binds
    @IntoMap
    @CommandKey(SubmissionsCommand::class)
    fun bindSubmissionsCommand(command: SubmissionsCommand): BaseCommand

    @Binds
    @IntoMap
    @CommandKey(VersionCommand::class)
    fun bindVersionCommand(command: VersionCommand): BaseCommand
}
