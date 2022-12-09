package at.fhooe.hagenberg.tutorbot.auth

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.util.promptPasswordInput
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialStore @Inject constructor(private val configHandler: ConfigHandler) {
    private var moodleUsername: String? = null
    private var moodlePassword: String? = null
    private var moodleCookie: String? = null
    private var emailAddress: String? = null
    private var emailUsername: String? = null
    private var emailPassword: String? = null

    // Read initial values from the config
    init {
        moodleUsername = configHandler.getMoodleUsername()
        moodlePassword = configHandler.getMoodlePassword()
        emailAddress = configHandler.getEmailAddress()
        emailUsername = configHandler.getEmailUsername()
        emailPassword = configHandler.getEmailPassword()
    }

    fun getMoodleUsername(): String {
        return moodleUsername ?: promptTextInput("Enter moodle username:").also { moodleUsername = it }
    }

    fun getEmailAddress(): String {
        return emailAddress ?: promptTextInput("Enter email address:").also { emailAddress = it }
    }

    fun getEmailUsername(): String {
        return emailUsername ?: promptTextInput("Enter email username:").also { emailUsername = it }
    }

    fun getMoodlePassword(): String {
        // return moodlePassword ?: promptTextInput("Enter moodle password ($moodleUsername):").also { moodlePassword = it }
        return moodlePassword ?: promptPasswordInput("Enter moodle password ($moodleUsername):").also {
            moodlePassword = it
        }
    }

    fun getMoodleCookie(): String {
        return moodleCookie
            ?: promptTextInput("Enter authorization cookie value (${configHandler.getMoodleCookieName()}):").also {
                moodleCookie = it
            }
    }

    fun getEmailPassword(): String {
        // return emailPassword ?: promptTextInput("Enter email password:").also { emailPassword = it }
        return emailPassword ?: promptPasswordInput("Enter email password:").also { emailPassword = it }
    }

    fun setEmailPassword(value: String?) {
        emailPassword = value
    }
}
