package at.fhooe.hagenberg.tutorbot.testutil

import org.junit.Assert.assertThrows

inline fun <reified T : Throwable> assertThrows(crossinline block: () -> Unit) {
    assertThrows(T::class.java) { block() }
}
