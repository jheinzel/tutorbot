package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.util.promptNumberInput
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ConfigHandler @Inject constructor(@Named("config") config: File) {
    enum class AuthMethod { USER_PASS, COOKIE }

    private val properties by lazy { parseProperties(config) }

    /* The following properties are either read from getProperty or prompted by console input.
       This is to avoid duplicating the prompt string in every command where the property is needed. */
    private var baseDir: String? = null
    private var exerciseDirectory: String? = null
    private var submissionsSubDir: String? = null
    private var reviewsSubDir: String? = null
    private var feedbackAmount: Int? = null
    private var feedbackRandomAmount: Int? = null
    private var feedbackCsv: String? = null

    fun getMoodleUsername(): String? = getProperty("moodle.username")
    fun getMoodlePassword(): String? = getProperty("moodle.password")
    fun getMoodleUrl(): String = getProperty("moodle.url") ?: "https://elearning.fh-ooe.at/"
    fun getMoodleAuthMethod(): AuthMethod =
        if (getProperty("moodle.auth.method")?.toLowerCase() == "cookie") AuthMethod.COOKIE else AuthMethod.USER_PASS
    fun getMoodleCookieName(): String = getProperty("moodle.cookie.name") ?: "MoodleSessionlmsfhooe"

    fun getEmailAddress(): String? = getProperty("email.address")
    fun getEmailUsername(): String? = getProperty("email.username")
    fun getEmailPassword(): String? = getProperty("email.password")
    fun getStudentsEmailSuffix(): String = getProperty("email.students.suffix") ?: "fhooe.at"
    fun getEmailSubjectTemplate(): String? = getProperty("email.template.subject")
    fun getEmailBodyTemplate(): String? = getProperty("email.template.body")

    fun getBaseDir(): String =
        baseDir ?: (getProperty("location.basedir") ?: promptTextInput("Enter base directory:")).also { baseDir = it }

    fun getExerciseSubDir(): String =
        exerciseDirectory ?: (getProperty("location.exercise.subdir")
            ?: promptTextInput("Enter exercise subdirectory:")).also { exerciseDirectory = it }

    fun getSubmissionsSubDir(): String =
        submissionsSubDir ?: (getProperty("location.submissions.subdir")
            ?: promptTextInput("Enter exercise subdirectory:")).also { submissionsSubDir = it }

    fun getReviewsSubDir(): String =
        reviewsSubDir ?: (getProperty("location.reviews.subdir")
            ?: promptTextInput("Enter reviews subdirectory:")).also { reviewsSubDir = it }

    fun getFeedbackAmount(): Int =
        feedbackAmount ?: (getProperty("feedback.amount")?.toIntOrNull()
            ?: promptNumberInput("Enter amount of reviews to pick:")).also { feedbackAmount = it }

    fun getFeedbackRandomAmount(): Int =
        feedbackRandomAmount ?: (getProperty("feedback.random.amount")?.toIntOrNull()
            ?: promptNumberInput("Enter amount of random reviews to pick (0 <= amount <= $feedbackAmount):")).also {
            feedbackRandomAmount = it
        }

    fun getFeedbackCsv(): String =
        feedbackCsv ?: (getProperty("feedback.csv")
            ?: promptTextInput("Enter CSV file with feedback counts (relative or absolute path):")).also {
            feedbackCsv = it
        }


    fun getJavaLanguageLevel(): String = getProperty("plagiarism.language.java.version") ?: "java19"

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
