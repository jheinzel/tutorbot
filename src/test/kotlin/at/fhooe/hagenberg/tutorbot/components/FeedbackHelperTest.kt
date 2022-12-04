package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.auth.MoodleAuthenticator
import at.fhooe.hagenberg.tutorbot.commands.ReviewsCommand
import at.fhooe.hagenberg.tutorbot.commands.SubmissionsCommand
import at.fhooe.hagenberg.tutorbot.network.MoodleClient
import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.getHtmlResource
import at.fhooe.hagenberg.tutorbot.testutil.getResource
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.nio.file.Path

class FeedbackHelperTest : CommandLineTest() {
    private val testFileName = "S1-S2_S1-S2.pdf"
    private val reviewLoc = "reviews"
    private val exerciseLoc = "ue01"

    private val configHandler = mockk<ConfigHandler> {
        // Ex. and review dir is same as base
        every { getExerciseSubDir() } returns "."
        every { getReviewsSubDir() } returns "."
        val pdfsLoc = ClassLoader.getSystemResource("pdfs").toString().split("file:/").last()
        every { getFeedbackDir() } returns pdfsLoc
        every { getBaseDir() } returns pdfsLoc
    }

    private val feedbackHelper = FeedbackHelper(configHandler)

    @Test
    fun `asdf`() {
        val b = feedbackHelper.readReviewsForExercise()
        //println()
    }
}