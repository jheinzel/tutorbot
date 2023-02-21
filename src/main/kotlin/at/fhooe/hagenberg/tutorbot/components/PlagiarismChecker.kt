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

    fun generatePlagiarismReport(submissionDirectory: File, outputDirectory: File) {
        val language = detectSubmissionLanguage(submissionDirectory)
        val reportDirectory = File(outputDirectory, REPORT_FOLDER)
        reportDirectory.mkdirs()
        val logFile = File(reportDirectory, LOG_FILE)

        // With subdirs (-s) verbose parser logging (-vp) and output to log file (-o)
        val args = arrayOf(
            "-l",
            language,
            "-r",
            reportDirectory.absolutePath,
            "-vp",
            "-o",
            logFile.absolutePath,
            "-s",
            submissionDirectory.absolutePath
        )

        // Capture output while running JPlag
        runWithCapturedOutput {
            jplagWrapper.run(args)
        }

        val reportFilePath = File(reportDirectory, INDEX_FILE).absolutePath
        println("Plagiarism output generated, you can check it here: $reportFilePath")
    }

    private fun detectSubmissionLanguage(submissionDirectory: File): String {
        val extensions = submissionDirectory.walkTopDown().filter(File::isFile).map { file -> file.extension }
        return when {
            extensions.contains("java") -> configHandler.getJavaLanguageLevel()
            extensions.contains("cpp") || extensions.contains("h") || extensions.contains("c") -> "c/c++"
            else -> exitWithError("Could not detect programming language for plagiarism detection")
        }
    }

    class JplagWrapper @Inject constructor() {
        fun run(args: Array<String>) {
            Program(CommandLineOptions(args)).run()
        }
    }

    companion object {
        const val REPORT_FOLDER = "plagiarism-report"
        const val LOG_FILE = "parser.log"
        const val INDEX_FILE = "index.html"
    }
}
