package at.fhooe.hagenberg.tutorbot.auth

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.network.UrlProvider
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.value
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodleAuthenticator @Inject constructor(
    private val http: OkHttpClient,
    private val credentialStore: CredentialStore,
    private val configHandler: ConfigHandler
) {
    private var authenticated = false

    fun authenticate() {
        if (authenticated) {
            return // No need to authenticate twice
        }

        // Reconstruct the Moodle login form
        val formBody = FormBody.Builder()
            .add("username", credentialStore.getMoodleUsername())
            .add("password", credentialStore.getMoodlePassword())
            .add("logintoken", getLoginToken()) // We need a valid login token for the request to be accepted
            .build()

        // Perform the login, the session authentication cookie is automatically stored
        val loginRequest = Request.Builder().url(configHandler.getMoodleUrl() + MOODLE_LOGIN_URL).post(formBody).build()
        val loginResponse = http.newCall(loginRequest).execute()

        // Make sure the authentication was successful
        if (!loginResponse.isSuccessful) {
            exitWithError("Could not authenticate, please check your internet connection.")
        }

        // If we got a login token again, we are not authenticated
        if (parseLoginTokenElement(loginResponse) != null) {
            exitWithError("Authentication failed, please check your credentials")
        }

        authenticated = true // Only authenticate once
    }

    private fun getLoginToken(): String {
        val tokenRequest = Request.Builder().url(configHandler.getMoodleUrl() + MOODLE_LOGIN_URL).build()
        val tokenResponse = http.newCall(tokenRequest).execute()
        val tokenElement = parseLoginTokenElement(tokenResponse)
        return tokenElement?.value() ?: exitWithError("Could not find login token, maybe Moodle changed its format")
    }

    private fun parseLoginTokenElement(response: Response): Element? {
        return Jsoup.parse(response.body?.string()).selectFirst("input[name=logintoken]")
    }

    private companion object {
        const val MOODLE_LOGIN_URL = "login/index.php"
    }
}
