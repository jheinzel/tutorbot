package at.fhooe.hagenberg.tutorbot.components

import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ConfigHandler @Inject constructor(@Named("config") config: File) {
    private val properties by lazy { parseProperties(config) }

    fun getUsername(): String? {
        return properties.getProperty("username")
    }

    fun getSubmissionsDownloadLocation(): String? {
        return properties.getProperty("location.submissions")
    }

    fun getReviewsDownloadLocation(): String? {
        return properties.getProperty("location.reviews")
    }

    private fun parseProperties(config: File): Properties {
        val properties = Properties()

        try {
            if (config.isFile) {
                properties.load(config.inputStream())
            }
        } catch (exception: Exception) {
            // Ignore parse exceptions -> just work with empty properties
        }

        return properties
    }
}
