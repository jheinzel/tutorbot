package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import io.mockk.*
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class PlagiarismCheckerTest : CommandLineTest() {
    private val submissionsLoc = "submissions"
    private val exerciseLoc = "ue01"

    private val jplagWrapper = mockk<PlagiarismChecker.JplagWrapper>()
    private val configHandler = mockk<ConfigHandler>()
    private val args = slot<Array<String>>()

    private val plagiarismChecker = PlagiarismChecker(jplagWrapper, configHandler)

    @get:Rule
    val fileSystem = FileSystemRule()

    private lateinit var submissionsDirFile: File
    private lateinit var outputDirFile: File

    @Before
    fun setup() {
        every { jplagWrapper.run(capture(args)) } just Runs
        submissionsDirFile = Path.of(fileSystem.directory.absolutePath, exerciseLoc, submissionsLoc).toFile()
        submissionsDirFile.mkdirs()
        outputDirFile = Path.of(fileSystem.directory.absolutePath, exerciseLoc).toFile()
    }

    @Test
    fun `JPlag is invoked correctly`() {
        File(submissionsDirFile, "test.java").createNewFile()
        plagiarismChecker.generatePlagiarismReport(submissionsDirFile, outputDirFile)

        val resultsPath = Paths.get(outputDirFile.path, PlagiarismChecker.REPORT_FOLDER).toString()
        val reportPath = Paths.get(resultsPath, PlagiarismChecker.INDEX_FILE).toString()
        val logFilePath = Path.of(outputDirFile.path, PlagiarismChecker.REPORT_FOLDER, PlagiarismChecker.LOG_FILE)
            .toString()

        val capturedArgs = args.captured.joinToString(separator = " ")
        // All arguments present, this test is not that useful
        assertTrue(
            capturedArgs.contains("-o") && capturedArgs.contains("-s") &&
                    capturedArgs.contains("-vp") && capturedArgs.contains("-r") && capturedArgs.contains("-l") &&
                    capturedArgs.contains(logFilePath) && capturedArgs.contains(resultsPath)
        )
        assertTrue(systemOut.log.contains(reportPath))
    }

    @Test
    fun `Java language version from config is used if available`() {
        every { configHandler.getJavaLanguageLevel() } returns "java11"
        File(submissionsDirFile, "test.java").createNewFile()
        plagiarismChecker.generatePlagiarismReport(submissionsDirFile, outputDirFile)

        assertTrue(args.captured.joinToString(separator = " ").contains("-l java11"))
    }

    @Test
    fun `C++ is detected correctly`() {
        File(submissionsDirFile, "test.cpp").createNewFile()
        plagiarismChecker.generatePlagiarismReport(submissionsDirFile, outputDirFile)

        assertTrue(args.captured.joinToString(separator = " ").contains("-l c/c++"))
    }

    @Test
    fun `Program exits if language could not be detected`() {
        assertThrows<ProgramExitError> {
            plagiarismChecker.generatePlagiarismReport(submissionsDirFile, outputDirFile)
        }
    }
}
