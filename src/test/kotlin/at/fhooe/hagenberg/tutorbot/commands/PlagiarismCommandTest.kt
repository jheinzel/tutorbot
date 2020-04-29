package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.PlagiarismChecker
import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import java.io.File

class PlagiarismCommandTest : CommandLineTest() {
    private val plagiarismChecker = mockk<PlagiarismChecker>()

    private val plagiarismCommand = PlagiarismCommand(plagiarismChecker)

    @get:Rule
    val fileSystem = FileSystemRule()

    @Test
    fun `Plagiarism checker is invoked correctly`() {
       systemIn.provideLines(fileSystem.directory.absolutePath)
        plagiarismCommand.execute()

        verify { plagiarismChecker.generatePlagiarismReport(fileSystem.directory) }
    }

    @Test
    fun `Program exits with error if the submissions directory is not valid`() {
        systemIn.provideLines(fileSystem.file.absolutePath)
        assertThrows<ProgramExitError> { plagiarismCommand.execute() }

        systemIn.provideLines(File(fileSystem.file, "nonexistant").absolutePath)
        assertThrows<ProgramExitError> { plagiarismCommand.execute() }
    }
}
