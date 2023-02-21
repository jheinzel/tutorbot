package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.PlagiarismChecker
import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.nio.file.Path

class PlagiarismCommandTest : CommandLineTest() {
    private val submissionsLoc = "submissions"
    private val exerciseLoc = "ue01"

    private val plagiarismChecker = mockk<PlagiarismChecker>()

    private val configHandler = mockk<ConfigHandler>() {
        every { getExerciseSubDir() } returns exerciseLoc
        every { getSubmissionsSubDir() } returns submissionsLoc
    }

    private val plagiarismCommand = PlagiarismCommand(plagiarismChecker, configHandler)

    @get:Rule
    val fileSystem = FileSystemRule()

    private lateinit var submissionsDirFile: File
    private lateinit var outputDirFile: File

    @Before
    fun setup() {
        every { configHandler.getBaseDir() } returns fileSystem.directory.absolutePath.toString()
        submissionsDirFile = Path.of(fileSystem.directory.absolutePath, exerciseLoc, submissionsLoc).toFile()
        outputDirFile = Path.of(fileSystem.directory.absolutePath, exerciseLoc).toFile()
    }

    @Test
    fun `Plagiarism checker is invoked correctly`() {
        submissionsDirFile.mkdirs()
        plagiarismCommand.execute()

        verify { plagiarismChecker.generatePlagiarismReport(submissionsDirFile, outputDirFile) }
    }

    @Test
    fun `Program exits with error if the submissions directory is not valid`() {
        assertThrows<ProgramExitError> { plagiarismCommand.execute() }
    }
}
