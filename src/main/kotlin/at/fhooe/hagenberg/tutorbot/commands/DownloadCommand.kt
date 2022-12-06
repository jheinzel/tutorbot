package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.promptBooleanInput
import java.io.File
import java.nio.file.Path

abstract class DownloadCommand(
    private val configHandler: ConfigHandler
) : BaseCommand() {

    protected abstract fun getCommandSubDir(): String?

    protected fun setupTargetDirectory(): File {
        val baseDir        = configHandler.getBaseDir()
        val exerciseSubDir = configHandler.getExerciseSubDir()
        val commandSubDir  = getCommandSubDir()
        val targetDirectory = Path.of(baseDir, exerciseSubDir, commandSubDir).toFile()

        // Make sure the target path points to a directory
        if (targetDirectory.isFile) {
            exitWithError("Download location $targetDirectory points to a file.")
        }

        // Make sure the directory is empty
        if (targetDirectory.exists()) {
            if (promptBooleanInput("Download location $targetDirectory is not empty, should its contents be deleted?")) {
                targetDirectory.deleteRecursively()
            } else {
                exitWithError("Cannot download into non-empty directory")
            }
        }
        targetDirectory.mkdirs() // Ensure directory exists

        return targetDirectory
    }
}
