package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import io.mockk.*
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.nio.file.Paths

class PlagiarismCheckerTest : CommandLineTest() {
    private val jplagWrapper = mockk<PlagiarismChecker.JplagWrapper>()
    private val args = slot<Array<String>>()

    private val plagiarismChecker = PlagiarismChecker(jplagWrapper)

    @get:Rule
    val fileSystem = FileSystemRule()

    @Before
    fun setup() {
        every { jplagWrapper.run(capture(args)) } just Runs
    }

    @Test
    fun `JPlag is invoked correctly`() {
        File(fileSystem.directory, "test.java").createNewFile()
        plagiarismChecker.generatePlagiarismReport(fileSystem.directory)

        val submissionPath = fileSystem.directory.absolutePath
        val resultsPath = Paths.get(submissionPath, "plagiarism-report").toString()
        val reportPath = Paths.get(resultsPath, "index.html").toString()
        assertTrue(args.captured.joinToString(separator = " ").contains("-r $resultsPath -s $submissionPath"))
        assertTrue(systemOut.log.contains(reportPath))
    }

    @Test
    fun `Java is detected correctly`() {
        File(fileSystem.directory, "test.java").createNewFile()
        plagiarismChecker.generatePlagiarismReport(fileSystem.directory)

        assertTrue(args.captured.joinToString(separator = " ").contains("-l java11"))
    }

    @Test
    fun `C++ is detected correctly`() {
        File(fileSystem.directory, "test.cpp").createNewFile()
        plagiarismChecker.generatePlagiarismReport(fileSystem.directory)

        assertTrue(args.captured.joinToString(separator = " ").contains("-l c/c++"))
    }

    @Test
    fun `Program exits if language could not be detected`() {
        assertThrows<ProgramExitError> {
            plagiarismChecker.generatePlagiarismReport(fileSystem.directory)
        }
    }
}
