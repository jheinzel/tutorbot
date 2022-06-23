package at.fhooe.hagenberg.tutorbot.components

import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ConfigHandler @Inject constructor(@Named("config") config: File) {
    private val properties by lazy { parseProperties(config) }

    fun getMoodleUsername(): String? = getProperty("moodle.username")
    fun getMoodleUrl(): String? = getProperty("moodle.url") ?: "https://elearning.fh-ooe.at/"

    fun getEmailAddress(): String? = getProperty("email.address")
    fun getEmailUsername(): String? = getProperty("email.username")
    fun getStudentsEmailSuffix(): String = getProperty("email.students.suffix") ?: "students.fh-hagenberg.at"

    fun getBaseDir(): String? = getProperty("location.basedir")
    fun getSubmissionsSubDir(): String? = getProperty("location.submissions.subdir")
    fun getReviewsSubDir(): String? = getProperty("location.reviews.subdir")
    fun getExerciseSubDir(): String? = getProperty("location.exercise.subdir")

    fun getJavaLanguageLevel(): String? = getProperty("plagiarism.language.java.version")

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

    private fun getProperty(key: String): String? {
        val property = properties.getProperty(key)
        if (property != null) {
            return property
        }

        // Read value from environment variables
        val envVarsKey = "TUTORBOT_" + key.replace('.', '_').toUpperCase(Locale.US)
        return System.getenv(envVarsKey)
    }
}
