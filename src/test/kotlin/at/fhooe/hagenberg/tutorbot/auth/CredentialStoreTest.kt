package at.fhooe.hagenberg.tutorbot.auth

import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.Console

class CredentialStoreTest : CommandLineTest() {
    private val console = mockk<Console>()
    private val configHandler = mockk<ConfigHandler> {
        every { getUsername() } returns null
    }

    private val credentialStore = CredentialStore(configHandler)

    @Before
    fun setup () {
        mockkStatic(System::class)
        every { System.console() } returns console
    }

    @After
    fun teardown() {
        unmockkStatic(System::class)
    }

    @Test
    fun `Reads initial username from config`() {
        every { configHandler.getUsername() } returns "ConfigUsername"
        val credentialStore = CredentialStore(configHandler)
        assertEquals("ConfigUsername", credentialStore.getUsername())
    }

    @Test
    fun `Retrieves username from cache or prompt`() {
        systemIn.provideLines("EnteredUsername")
        repeat(3) { // Subsequent queries should be cached
            assertEquals("EnteredUsername", credentialStore.getUsername())
        }
    }

    @Test
    fun `Retrieves password from cache or prompt`() {
        every { console.readPassword() } returns "EnteredPassword".toCharArray()
        repeat(3) { // Subsequent queries should be cached
            assertEquals("EnteredPassword", credentialStore.getPassword())
        }
    }
}
