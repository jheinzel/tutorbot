package at.fhooe.hagenberg.tutorbot.testutil

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File

fun getResource(name: String): File {
    return File(ClassLoader.getSystemResource(name).toURI())
}

fun getHtmlResource(name: String): Document {
    return Jsoup.parse(getResource(name).readText())
}
