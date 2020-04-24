package at.fhooe.hagenberg.tutorbot.auth

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.util.promptPasswordInput
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialStore @Inject constructor(configHandler: ConfigHandler) {
    private var username: String? = null
    private var password: String? = null

    // Read initial values from the config
    init {
        username = configHandler.getUsername()
    }

    fun getUsername(): String {
        return username ?: promptTextInput("Enter Moodle username:").also { username = it }
    }

    fun getPassword(): String {
        return password ?: promptPasswordInput("Enter Moodle password:").also { password = it }
    }
}
