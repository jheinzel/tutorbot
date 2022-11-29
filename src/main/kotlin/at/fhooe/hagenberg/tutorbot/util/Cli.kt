package at.fhooe.hagenberg.tutorbot.util

import java.io.OutputStream
import java.io.PrintStream

fun promptTextInput(prompt: String): String {
    print("$prompt ")
    return readLine() ?: ""
}

fun promptMultilineTextInput(prompt: String): String {
    val textInput = promptTextInput("$prompt (use <br/> for line breaks)")
    return textInput.trim().replace("<br/>", System.lineSeparator())
}

fun promptPasswordInput(prompt: String): String {
    print("$prompt ")
    return System.console().readPassword().joinToString(separator = "")
}

fun promptBooleanInput(prompt: String): Boolean {
    val positiveAnswers = listOf("", "y", "yes")
    val textInput = promptTextInput("$prompt [Y/N]")
    return textInput.trim().toLowerCase() in positiveAnswers
}

fun promptNumberInput(prompt: String): Int {
    print("$prompt ")
    var nr: Int?
    do {
        nr = readLine()?.toIntOrNull()
        if(nr == null) printlnRed("ERROR: Not a number, please try again.")
    }while(nr == null)
    return nr
}

fun exitWithError(message: String): Nothing {
    printlnRed("ERROR: $message")
    throw ProgramExitError()
}

inline fun runWithCapturedOutput(block: () -> Unit) {
    val out = System.out
    val err = System.err

    // Redirect all outputs to a dummy stream
    val dummyStream = PrintStream(OutputStream.nullOutputStream())
    System.setOut(dummyStream)
    System.setErr(dummyStream)

    block() // Execute the code

    // Restore original streams
    System.setOut(out)
    System.setErr(err)
}

class ProgramExitError : Error()
