package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.nio.file.Paths

class UnzipperTest {
    private val unzipper = Unzipper()

    @get:Rule
    val fileSystem = FileSystemRule()

    @Test
    fun `File gets unzipped correctly`() {
        val targetFile = File(fileSystem.directory, "pdfs.zip")
        File(ClassLoader.getSystemResource("zip/pdfs.zip").toURI()).copyTo(targetFile)

        unzipper.unzipFile(targetFile)

        val basePath = Paths.get(fileSystem.directory.absolutePath, "pdfs")
        assertTrue(basePath.resolve("1.pdf").toFile().exists())
        assertTrue(basePath.resolve("2.pdf").toFile().exists())
    }
}
