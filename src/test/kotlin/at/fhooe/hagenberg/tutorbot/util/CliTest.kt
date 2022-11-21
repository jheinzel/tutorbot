package at.fhooe.hagenberg.tutorbot.util

import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.Console

class CliTest : CommandLineTest() {
    private val console = mockk<Console>()

    @Before
    fun setup () {
        mockkStatic(System::class)
        every { System.console() } returns console
    }

    @After
    fun teardown() {
        unmockkStatic(System::class)
    }

    @Test
    fun `Text prompts work correctly`() {
        systemIn.provideLines("Input")
        assertEquals("Input", promptTextInput("Enter:"))
        assertEquals("Enter: ", systemOut.log)
    }

    @Test
    fun `Multiline text prompts work correctly`() {
        val newline = System.lineSeparator()
        systemIn.provideLines("This<br/>is<br/>a<br/>multiline<br/>text")
        assertEquals("This${newline}is${newline}a${newline}multiline${newline}text", promptMultilineTextInput("Enter:"))
        assertEquals("Enter: (use <br/> for line breaks) ", systemOut.log)
    }

    @Test
    fun `Password prompts work correctly`() {
        every { console.readPassword() } returns "Password".toCharArray()
        assertEquals("Password", promptPasswordInput("Enter:"))
        assertEquals("Enter: ", systemOut.log)
    }

    @Test
    fun `Boolean prompts work correctly`() {
        val positiveAnswers = listOf("", "y", "Y", "yes", "YES")
        val negativeAnswers = listOf("n", "N", "no", "NO", "abc")

        positiveAnswers.forEach { answer ->
            systemIn.provideLines(answer)
            assertTrue(promptBooleanInput("Enter:"))
            assertEquals("Enter: [Y/N] ", systemOut.log)
            systemOut.clearLog()
        }

        negativeAnswers.forEach { answer ->
            systemIn.provideLines(answer)
            assertFalse(promptBooleanInput("Enter:"))
            assertEquals("Enter: [Y/N] ", systemOut.log)
            systemOut.clearLog()
        }
    }

    @Test
    fun `Exiting the program works correctly`() {
        assertThrows<ProgramExitError> { exitWithError("Error message") }
    }

    @Test
    fun `Capturing output works correctly`() {
        runWithCapturedOutput {
            print("Normal output #1")
            System.err.print("Error output #1")
        }
        assertEquals("", systemOut.log)
        assertEquals("", systemErr.log)

        print("Normal output #2")
        System.err.print("Error output #2")
        assertEquals("Normal output #2", systemOut.log)
        assertEquals("Error output #2", systemErr.log)
    }
}
