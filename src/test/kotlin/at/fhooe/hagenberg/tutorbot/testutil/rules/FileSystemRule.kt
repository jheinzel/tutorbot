package at.fhooe.hagenberg.tutorbot.testutil.rules

import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.nio.file.Files

class FileSystemRule : TestWatcher() {
    private val prefix = "tutorbot"
    private val suffix = "test"

    lateinit var directory: File
        private set

    lateinit var file: File
        private set

    override fun starting(description: Description?) {
        directory = Files.createTempDirectory(prefix).toFile()
        file = File.createTempFile(prefix, suffix, directory)
    }

    override fun finished(description: Description?) {
        directory.deleteRecursively()
    }
}
