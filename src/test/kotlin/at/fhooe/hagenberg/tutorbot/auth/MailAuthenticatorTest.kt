package at.fhooe.hagenberg.tutorbot.auth

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class MailAuthenticatorTest {
    private val credentialStore = mockk<CredentialStore>()
    
    private val mailAuthenticator = MailAuthenticator(credentialStore)
    
    @Test
    fun `Password authentication uses correct credentials`() {
        every { credentialStore.getUsername() } returns "username"
        every { credentialStore.getPassword() } returns "password"

        val auth = mailAuthenticator.passwordAuthentication

        assertEquals("username", auth.userName)
        assertEquals("password", auth.password)
    }
}
