package at.fhooe.hagenberg.tutorbot.util

import org.jsoup.nodes.Element

fun Element.value(): String = `val`()

fun Element.href(): String = attr("href")
