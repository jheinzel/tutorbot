package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.promptBooleanInput
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import java.io.File

abstract class DownloadCommand : BaseCommand() {

    protected abstract fun getTargetDirectoryFromConfig(): String?

    protected fun setupTargetDirectory(): File {
        val locationPrompt = "Enter download location (leave empty for current directory):"
        val targetPath = getTargetDirectoryFromConfig() ?: promptTextInput(locationPrompt)
        val targetDirectory = File(targetPath)

        // Make sure the target path points to a directory
        if (targetDirectory.isFile) {
            exitWithError("Download location $targetPath points to a file.")
        }

        // Make sure the directory is empty
        if (targetDirectory.exists()) {
            if (promptBooleanInput("Download location $targetPath is not empty, should its contents be deleted?")) {
                targetDirectory.deleteRecursively()
            } else {
                exitWithError("Cannot download into non-empty directory")
            }
        }
        targetDirectory.mkdirs() // Ensure directory exists

        return targetDirectory
    }
}
