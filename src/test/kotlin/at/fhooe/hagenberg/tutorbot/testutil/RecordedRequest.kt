package at.fhooe.hagenberg.tutorbot.testutil

import okhttp3.mockwebserver.RecordedRequest
import java.net.URLDecoder

fun RecordedRequest.getFormValue(name: String): String {
    val formBody = body.clone().readUtf8().split("&").associate { entry ->
        val (key, value) = entry.split("=").map { part -> URLDecoder.decode(part, "UTF-8") }
        key to value
    }
    return formBody.getValue(name)
}
