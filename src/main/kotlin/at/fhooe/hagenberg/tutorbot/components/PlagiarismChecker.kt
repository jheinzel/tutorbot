package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.runWithCapturedOutput
import jplag.Program
import jplag.options.CommandLineOptions
import java.io.File
import javax.inject.Inject

class PlagiarismChecker @Inject constructor(
    private val jplagWrapper: JplagWrapper,
    private val configHandler: ConfigHandler
) {

    fun generatePlagiarismReport(submissionDirectory: File) {
        val language = detectSubmissionLanguage(submissionDirectory)
        val reportDirectory = File(submissionDirectory, "plagiarism-report")
        val args = arrayOf("-l", language, "-r", reportDirectory.absolutePath, "-s", submissionDirectory.absolutePath)

        // Capture output while running JPlag
        runWithCapturedOutput {
            jplagWrapper.run(args)
        }
        val reportFilePath = File(reportDirectory, "index.html").absolutePath
        println("Plagiarism output generated, you can check it here: $reportFilePath")
    }

    private fun detectSubmissionLanguage(submissionDirectory: File): String {
        val extensions = submissionDirectory.walkTopDown().filter(File::isFile).map { file -> file.extension }
        return when {
            extensions.contains("java") -> configHandler.getJavaLanguageLevel() ?: "java19"
            extensions.contains("cpp") || extensions.contains("h") || extensions.contains("c") -> "c/c++"
            else -> exitWithError("Could not detect programming language for plagiarism detection")
        }
    }

    class JplagWrapper @Inject constructor() {
        fun run(args: Array<String>) {
            Program(CommandLineOptions(args)).run()
        }
    }
}
