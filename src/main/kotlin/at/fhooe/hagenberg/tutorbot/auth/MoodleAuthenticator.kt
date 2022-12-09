package at.fhooe.hagenberg.tutorbot.auth

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.printlnGreen
import at.fhooe.hagenberg.tutorbot.util.value
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URI
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

        val response = when (configHandler.getMoodleAuthMethod()) {
            ConfigHandler.AuthMethod.COOKIE -> authenticateUsingCookie()
            ConfigHandler.AuthMethod.USER_PASS -> authenticateUsingUserPass()
        }

        response.use {
            // Make sure the authentication was successful
            if (!response.isSuccessful) {
                exitWithError("Could not authenticate, please check your internet connection.")
            }

            // If we got a login token again, we are not authenticated
            if (parseLoginTokenElement(response) != null) {
                exitWithError("Authentication failed, please check your credentials.")
            }

            authenticated = true // Only authenticate once
            printlnGreen("Authentication successful")
        }
    }

    private fun authenticateUsingUserPass(): Response {
        // Reconstruct the Moodle login form
        val formBody = FormBody.Builder()
            .add("username", credentialStore.getMoodleUsername())
            .add("password", credentialStore.getMoodlePassword())
            .add("logintoken", getLoginToken()) // We need a valid login token for the request to be accepted
            .build()

        // Perform the login, the session authentication cookie is automatically stored
        val loginRequest = Request.Builder().url(configHandler.getMoodleUrl() + MOODLE_LOGIN_URL).post(formBody).build()
        return http.newCall(loginRequest).execute()
    }

    private fun authenticateUsingCookie(): Response {
        http.cookieJar.saveFromResponse(
            configHandler.getMoodleUrl().toHttpUrl(),
            listOf(createMoodleCookie(credentialStore.getMoodleCookie()))
        )

        // Test request will be redirected to /login if cookie is invalid
        val homeUrl = configHandler.getMoodleUrl()
        val request = Request.Builder().url(homeUrl).build()
        return http.newCall(request).execute()
    }

    private fun createMoodleCookie(value: String): Cookie {
        // Domain has to exclude www., use moodle url as input
        val domain = URI(configHandler.getMoodleUrl()).host.split("www.").last()
        return Cookie.Builder()
            .hostOnlyDomain(domain)
            .path("/")
            .name(configHandler.getMoodleCookieName())
            .value(value)
            .secure()
            .build()
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
