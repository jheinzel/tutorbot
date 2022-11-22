package at.fhooe.hagenberg.tutorbot.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables
import java.io.File

class ConfigHandlerTest {

    @get:Rule
    val envVars = EnvironmentVariables()

    @Test
    fun `All properties get read correctly`() {
        val config = File(ClassLoader.getSystemResource("config/all.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals("config-moodle-username", configHandler.getMoodleUsername())
        assertEquals("config-moodle-password", configHandler.getMoodlePassword())
        assertEquals("config-moodle-url", configHandler.getMoodleUrl())
        assertEquals("config-email-address", configHandler.getEmailAddress())
        assertEquals("config-email-username", configHandler.getEmailUsername())
        assertEquals("config-email-password", configHandler.getEmailPassword())
        assertEquals("config-email-suffix", configHandler.getStudentsEmailSuffix())
        assertEquals("config-email-subject", configHandler.getEmailSubjectTemplate())
        assertEquals("config-email-body", configHandler.getEmailBodyTemplate())
        assertEquals("config-base-dir", configHandler.getBaseDir())
        assertEquals("config-submissions", configHandler.getSubmissionsSubDir())
        assertEquals("config-reviews", configHandler.getReviewsSubDir())
        assertEquals("config-exercise", configHandler.getExerciseSubDir())
        assertEquals("config-plagiarism-language-java-version", configHandler.getJavaLanguageLevel())
    }

    @Test
    fun `Environment variables get read correctly`() {
        envVars.set("TUTORBOT_MOODLE_USERNAME", "env-username")
        envVars.set("TUTORBOT_PLAGIARISM_LANGUAGE_JAVA_VERSION", "env-plagiarism-language-java-version")

        val config = File(ClassLoader.getSystemResource("config/empty.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals("env-username", configHandler.getMoodleUsername())
        assertEquals("env-plagiarism-language-java-version", configHandler.getJavaLanguageLevel())
    }

    @Test
    fun `Properties take precedence over environment variables`() {
        envVars.set("TUTORBOT_MOODLE_USERNAME", "env-username")

        val config = File(ClassLoader.getSystemResource("config/all.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals("config-moodle-username", configHandler.getMoodleUsername())
    }

    @Test
    fun `Missing properties are handled correctly`() {
        val config = File(ClassLoader.getSystemResource("config/missing.properties").toURI())
        val configHandler = ConfigHandler(config)

        // Present property is found successfully, others should be null
        assertEquals("config-username", configHandler.getMoodleUsername())
        assertNull(configHandler.getBaseDir())
        assertNull(configHandler.getJavaLanguageLevel())
    }

    @Test
    fun `Parse exceptions are handled correctly`() {
        val config = File(ClassLoader.getSystemResource("config/corrupt.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertNull(configHandler.getMoodleUsername())
//        assertNull(configHandler.getSubmissionsDownloadLocation())
//        assertNull(configHandler.getReviewsDownloadLocation())
        assertNull(configHandler.getJavaLanguageLevel())
    }
}
