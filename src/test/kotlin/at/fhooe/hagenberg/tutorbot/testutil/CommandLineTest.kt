package at.fhooe.hagenberg.tutorbot.testutil

import org.junit.Rule
import org.junit.contrib.java.lang.system.SystemErrRule
import org.junit.contrib.java.lang.system.SystemOutRule
import org.junit.contrib.java.lang.system.TextFromStandardInputStream

/** Base test class for all unit tests that need to simulate command line interactions. */
abstract class CommandLineTest {

    @get:Rule
    val systemOut: SystemOutRule = SystemOutRule().enableLog().muteForSuccessfulTests()

    @get:Rule
    val systemErr: SystemErrRule = SystemErrRule().enableLog().muteForSuccessfulTests()

    @get:Rule
    val systemIn: TextFromStandardInputStream = TextFromStandardInputStream.emptyStandardInputStream()
}
