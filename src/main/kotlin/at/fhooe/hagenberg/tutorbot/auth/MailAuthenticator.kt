package at.fhooe.hagenberg.tutorbot.auth

import javax.inject.Inject
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication

class MailAuthenticator @Inject constructor(
    private val credentialStore: CredentialStore
) : Authenticator() {

    public override fun getPasswordAuthentication(): PasswordAuthentication {
        val username = credentialStore.getEmailUsername()
        val password = credentialStore.getEmailPassword()
        return PasswordAuthentication(username, password)
    }
}
