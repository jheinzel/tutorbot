package at.fhooe.hagenberg.tutorbot.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class ConfigHandlerTest {

    @Test
    fun `Properties get read correctly`() {
        val config = File(ClassLoader.getSystemResource("config/all.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals("config-username", configHandler.getUsername())
        assertEquals("config-location-submissions", configHandler.getSubmissionsDownloadLocation())
        assertEquals("config-location-reviews", configHandler.getReviewsDownloadLocation())
        assertEquals("config-plagiarism-language-java-version", configHandler.getJavaLanguageLevel())
    }

    @Test
    fun `Missing properties are handled correctly`() {
        val config = File(ClassLoader.getSystemResource("config/missing.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertEquals("config-username", configHandler.getUsername())
        assertNull(configHandler.getSubmissionsDownloadLocation())
        assertNull(configHandler.getReviewsDownloadLocation())
        assertNull(configHandler.getJavaLanguageLevel())
    }

    @Test
    fun `Parse exceptions are handled correctly`() {
        val config = File(ClassLoader.getSystemResource("config/corrupt.properties").toURI())
        val configHandler = ConfigHandler(config)

        assertNull(configHandler.getUsername())
        assertNull(configHandler.getSubmissionsDownloadLocation())
        assertNull(configHandler.getReviewsDownloadLocation())
        assertNull(configHandler.getJavaLanguageLevel())
    }
}
