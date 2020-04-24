package at.fhooe.hagenberg.tutorbot.network

import at.fhooe.hagenberg.tutorbot.auth.MoodleAuthenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import javax.inject.Inject

class MoodleClient @Inject constructor(
    private val http: OkHttpClient,
    private val authenticator: MoodleAuthenticator
) {
    fun getHtmlDocument(url: String): Document {
        authenticator.authenticate()

        val request = Request.Builder().url(url).build()
        val response = http.newCall(request).execute()

        return Jsoup.parse(response.body?.string())
    }

    fun downloadFile(url: String, target: File) {
        authenticator.authenticate()

        val request = Request.Builder().url(url).build()
        val response = http.newCall(request).execute()

        response.body?.let { body ->
            target.sink().buffer().use { sink -> sink.writeAll(body.source()) }
        }
    }
}
