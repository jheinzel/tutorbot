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
    fun `Properties get read correctly`() {
        val config = File(ClassLoader.getSystemResource("config/all.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals("config-username", configHandler.getMoodleUsername())
        assertEquals("config-email-address", configHandler.getEmailAddress())
        assertEquals("config-email-username", configHandler.getEmailUsername())
        assertEquals("config-basedir", configHandler.getBaseDir())
        assertEquals("config-submissions", configHandler.getSubmissionsSubDir())
        assertEquals("config-reviews", configHandler.getReviewsSubDir())
        assertEquals("config-exercise", configHandler.getExerciseSubDir())
        assertEquals("config-plagiarism-language-java-version", configHandler.getJavaLanguageLevel())
    }

    @Test
    fun `Environment variables get read correctly`() {
        envVars.set("TUTORBOT_USERNAME", "env-username")
        envVars.set("TUTORBOT_LOCATION_SUBMISSIONS", "env-location-submissions")
        envVars.set("TUTORBOT_LOCATION_REVIEWS", "env-location-reviews")
        envVars.set("TUTORBOT_PLAGIARISM_LANGUAGE_JAVA_VERSION", "env-plagiarism-language-java-version")

        val config = File(ClassLoader.getSystemResource("config/empty.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals("env-username", configHandler.getMoodleUsername())
//        assertEquals("env-location-submissions", configHandler.getSubmissionsDownloadLocation())
//        assertEquals("env-location-reviews", configHandler.getReviewsDownloadLocation())
        assertEquals("env-plagiarism-language-java-version", configHandler.getJavaLanguageLevel())
    }

    @Test
    fun `Properties take precedence over environment variables`() {
        envVars.set("TUTORBOT_USERNAME", "env-username")

        val config = File(ClassLoader.getSystemResource("config/all.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals("config-username", configHandler.getMoodleUsername())
    }

    @Test
    fun `Missing properties are handled correctly`() {
        val config = File(ClassLoader.getSystemResource("config/missing.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals("config-username", configHandler.getMoodleUsername())
//        assertNull(configHandler.getSubmissionsDownloadLocation())
//        assertNull(configHandler.getReviewsDownloadLocation())
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
