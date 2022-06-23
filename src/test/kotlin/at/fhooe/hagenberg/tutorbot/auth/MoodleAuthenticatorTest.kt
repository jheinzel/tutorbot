package at.fhooe.hagenberg.tutorbot.auth

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.rules.MockServerRule
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import at.fhooe.hagenberg.tutorbot.testutil.getFormValue
import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import io.mockk.every
import io.mockk.mockk
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MoodleAuthenticatorTest : CommandLineTest() {
    private val http = OkHttpClient()
    private val credentialStore = mockk<CredentialStore> {
        every { getMoodleUsername() } returns "moodle-username"
        every { getEmailPassword() } returns "moodle-password"
    }
    private val configHandler = mockk<ConfigHandler>()

    @get:Rule
    val mockServer = MockServerRule()

    private val moodleAuthenticator = MoodleAuthenticator(http, credentialStore, configHandler)

    @Test
    fun `Login is performed if not yet authenticated`() {
        mockServer.enqueueResource("websites/LoggedOut.html")
        mockServer.enqueueResource("websites/Blank.html")
        mockServer.start()

        moodleAuthenticator.authenticate()

        val loginTokenRequest = mockServer.takeRequest()
        assertEquals("/login/index.php", loginTokenRequest.path)
        assertEquals("GET", loginTokenRequest.method)

        val authenticationRequest = mockServer.takeRequest()
        assertEquals("/login/index.php", authenticationRequest.path)
        assertEquals("POST", authenticationRequest.method)
        assertEquals("moodle-username", authenticationRequest.getFormValue("username"))
        assertEquals("moodle-password", authenticationRequest.getFormValue("password"))
        assertEquals("secrettoken", authenticationRequest.getFormValue("logintoken"))
    }

    @Test
    fun `No action is taken if already authenticated`() {
        mockServer.enqueueResource("websites/LoggedOut.html")
        mockServer.enqueueResource("websites/Blank.html")
        mockServer.start()
        moodleAuthenticator.authenticate() // Perform initial login

        repeat(5) { // Subsequent login calls should do nothing
            moodleAuthenticator.authenticate()
        }
    }

    @Test
    fun `Program exits if login token request fails`() {
        mockServer.enqueueResponseCode(500)
        mockServer.start()

        assertThrows<ProgramExitError> { moodleAuthenticator.authenticate() }
    }

    @Test
    fun `Program exits if authentication request fails`() {
        mockServer.enqueueResource("websites/LoggedOut.html")
        mockServer.enqueueResponseCode(500)
        mockServer.start()

        assertThrows<ProgramExitError> { moodleAuthenticator.authenticate() }
    }

    @Test
    fun `Program exits when wrong credentials are used`() {
        mockServer.enqueueResource("websites/LoggedOut.html")
        mockServer.enqueueResource("websites/LoggedOut.html")
        mockServer.start()

        assertThrows<ProgramExitError> { moodleAuthenticator.authenticate() }
    }

    @Test
    fun `Program exits if the page format changed`() {
        mockServer.enqueueResource("websites/Blank.html")
        mockServer.start()

        assertThrows<ProgramExitError> { moodleAuthenticator.authenticate() }
    }
}
