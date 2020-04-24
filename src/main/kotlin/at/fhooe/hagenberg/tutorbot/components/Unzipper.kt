package at.fhooe.hagenberg.tutorbot.components

import java.io.*
import java.util.zip.ZipFile
import javax.inject.Inject

class Unzipper @Inject constructor() {

    fun unzipFile(file: File) = try {
        val parentDir = File(file.parentFile, file.nameWithoutExtension)

        // Unzip the file
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val target = File(parentDir, entry.name)

                if (!entry.isDirectory) {
                    extractFile(zipFile.getInputStream(entry), target)
                }
            }
        }

        // Special handling for macOS archives
        File(parentDir, "__MACOSX").deleteRecursively()
    } catch (exception: Exception) { /* Ignore errors */ }

    // Extracts a single file into the specified location
    private fun extractFile(zipInputStream: InputStream, target: File) {
        target.parentFile.mkdirs() // Ensure the parent directory exists
        BufferedInputStream(zipInputStream).use { inputStream ->
            FileOutputStream(target).use { outputStream ->
                while (inputStream.available() > 0) {
                    outputStream.write(inputStream.read())
                }
            }
        }
    }
}
