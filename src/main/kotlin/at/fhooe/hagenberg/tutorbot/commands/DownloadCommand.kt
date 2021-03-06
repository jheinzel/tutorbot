package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.promptBooleanInput
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import java.io.File
import java.nio.file.Path

abstract class DownloadCommand(
    private val configHandler: ConfigHandler
) : BaseCommand() {

    protected abstract fun getCommandSubDir(): String?

    protected fun setupTargetDirectory(): File {
        val baseDir        = configHandler.getBaseDir()        ?: promptTextInput("Enter base directory:")
        val exerciseSubDir = configHandler.getExerciseSubDir() ?: promptTextInput("Enter exercise subdirectory:")
        configHandler.setExerciseSubDir(exerciseSubDir) // prevents asking for exercise directory when downloading submissions in review command
        val commandSubDir  = getCommandSubDir()
        val targetDirectory = Path.of(baseDir, exerciseSubDir, commandSubDir).toFile()

        // Make sure the target path points to a directory
        if (targetDirectory.isFile) {
            exitWithError("Download location ${targetDirectory.toString()} points to a file.")
        }

        // Make sure the directory is empty
        if (targetDirectory.exists()) {
            if (promptBooleanInput("Download location ${targetDirectory.toString()} is not empty, should its contents be deleted?")) {
                targetDirectory.deleteRecursively()
            } else {
                exitWithError("Cannot download into non-empty directory")
            }
        }
        targetDirectory.mkdirs() // Ensure directory exists

        return targetDirectory
    }
}
