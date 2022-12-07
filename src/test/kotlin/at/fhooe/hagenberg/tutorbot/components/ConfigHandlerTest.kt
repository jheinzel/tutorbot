package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.contrib.java.lang.system.EnvironmentVariables
import java.io.File

class ConfigHandlerTest : CommandLineTest() {

    @get:Rule
    val envVars = EnvironmentVariables()

    @Test
    fun `All properties get read correctly`() {
        val config = File(ClassLoader.getSystemResource("config/all.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals("config-moodle-username", configHandler.getMoodleUsername())
        assertEquals("config-moodle-password", configHandler.getMoodlePassword())
        assertEquals("config-moodle-url", configHandler.getMoodleUrl())
        assertEquals(ConfigHandler.AuthMethod.COOKIE, configHandler.getMoodleAuthMethod())
        assertEquals("config-email-address", configHandler.getEmailAddress())
        assertEquals("config-email-username", configHandler.getEmailUsername())
        assertEquals("config-email-password", configHandler.getEmailPassword())
        assertEquals("config-cookie-name", configHandler.getMoodleCookieName())
        assertEquals("config-email-suffix", configHandler.getStudentsEmailSuffix())
        assertEquals("config-email-subject", configHandler.getEmailSubjectTemplate())
        assertEquals("config-email-body", configHandler.getEmailBodyTemplate())
        assertEquals("config-base-dir", configHandler.getBaseDir())
        assertEquals("config-submissions", configHandler.getSubmissionsSubDir())
        assertEquals("config-reviews", configHandler.getReviewsSubDir())
        assertEquals("config-exercise", configHandler.getExerciseSubDir())
        assertEquals(10, configHandler.getFeedbackAmount())
        assertEquals(3, configHandler.getFeedbackRandomAmount())
        assertEquals("config-feedback-csv", configHandler.getFeedbackCsv())
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

        // Present property is found successfully, others should be null or have their default value
        assertEquals("config-username", configHandler.getMoodleUsername())
        assertNull(configHandler.getMoodlePassword())
        assertNull(configHandler.getEmailUsername())
        assert(configHandler.getJavaLanguageLevel().isNotBlank())
        assert(configHandler.getMoodleUrl().isNotBlank())
    }

    @Test
    fun `Moodle authorization config defaults to USER_PASS when not present`() {
        val config = File(ClassLoader.getSystemResource("config/missing.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals(ConfigHandler.AuthMethod.USER_PASS, configHandler.getMoodleAuthMethod())
    }

    @Test
    fun `Parse exceptions are handled correctly`() {
        val config = File(ClassLoader.getSystemResource("config/corrupt.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertNull(configHandler.getMoodleUsername()) // Still works, throws no ex.
    }

    @Test
    fun `Prompted are read from console if not provided otherwise`() {
        val config = File(ClassLoader.getSystemResource("config/missing.properties").toURI())
        val configHandler = ConfigHandler(config)

        systemIn.provideLines("base-dir", "ex-dir", "sub-dir", "rev-dir", "3", "1", "fed-csv")
        assertEquals("base-dir", configHandler.getBaseDir())
        assertEquals("ex-dir", configHandler.getExerciseSubDir())
        assertEquals("sub-dir", configHandler.getSubmissionsSubDir())
        assertEquals("rev-dir", configHandler.getReviewsSubDir())
        assertEquals(3, configHandler.getFeedbackAmount())
        assertEquals(1, configHandler.getFeedbackRandomAmount())
        assertEquals("fed-csv", configHandler.getFeedbackCsv())
    }

    @Test
    fun `Prompted properties save value after prompted once`() {
        val config = File(ClassLoader.getSystemResource("config/missing.properties").toURI())
        val configHandler = ConfigHandler(config)

        systemIn.provideLines("base-dir", "ex-dir", "sub-dir", "rev-dir", "3", "1", "fed-csv")
        configHandler.getBaseDir()
        configHandler.getExerciseSubDir()
        configHandler.getSubmissionsSubDir()
        configHandler.getReviewsSubDir()
        configHandler.getFeedbackAmount()
        configHandler.getFeedbackRandomAmount()
        configHandler.getFeedbackCsv()

        assertEquals("base-dir", configHandler.getBaseDir())
        assertEquals("ex-dir", configHandler.getExerciseSubDir())
        assertEquals("sub-dir", configHandler.getSubmissionsSubDir())
        assertEquals("rev-dir", configHandler.getReviewsSubDir())
        assertEquals(3, configHandler.getFeedbackAmount())
        assertEquals(1, configHandler.getFeedbackRandomAmount())
        assertEquals("fed-csv", configHandler.getFeedbackCsv())
    }
}
