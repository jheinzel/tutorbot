package at.fhooe.hagenberg.tutorbot.network

import at.fhooe.hagenberg.tutorbot.auth.MoodleAuthenticator
import at.fhooe.hagenberg.tutorbot.testutil.rules.MockServerRule
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import io.mockk.mockk
import io.mockk.verify
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MoodleClientTest {
    private val http = OkHttpClient()
    private val moodleAuthenticator = mockk<MoodleAuthenticator>()

    private val moodleClient = MoodleClient(http, moodleAuthenticator)

    @get:Rule
    val mockServer = MockServerRule()

    @get:Rule
    val fileSystem = FileSystemRule()

    @Test
    fun `Requesting html documents works correctly`() {
        mockServer.enqueueResource("websites/Blank.html")
        mockServer.start()

        val document = moodleClient.getHtmlDocument(mockServer.baseUrl())
        verify { moodleAuthenticator.authenticate() }

        assertEquals("Blank", document.title())
    }

    @Test
    fun `Downloading file works correctly`() {
        mockServer.enqueueResource("websites/Blank.html")
        mockServer.start()

        moodleClient.downloadFile(mockServer.baseUrl(), fileSystem.file)
        verify { moodleAuthenticator.authenticate() }

        val content = ClassLoader.getSystemResource("websites/Blank.html").readText()
        assertEquals(content, fileSystem.file.readText())
    }
}
