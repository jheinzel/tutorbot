package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.FeedbackFileHelper
import at.fhooe.hagenberg.tutorbot.domain.FeedbackCount
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SaveFeedbackCommandTest {
    private val reviewSubDir = "reviews"
    private val exerciseSubDir = "ue01"
    private val reviewsInFolder = mapOf(
        "S1" to FeedbackCount(1, 0),
        "S2" to FeedbackCount(0, 1)
    )

    private val feedbackCountWriteCapture = slot<Map<String, FeedbackCount>>()

    private val configHandler = mockk<ConfigHandler> {
        every { getExerciseSubDir() } returns exerciseSubDir
        every { getReviewsSubDir() } returns reviewSubDir
    }
    private val feedbackFileHelper = mockk<FeedbackFileHelper> {
        every { readFeedbackCountFromReviews(any()) } returns reviewsInFolder
        every { writeFeedbackCountToCsv(any(), capture(feedbackCountWriteCapture)) } returns Unit
    }

    private val saveFeedbackCommand = SaveFeedbackCommand(configHandler, feedbackFileHelper)

    @get:Rule
    val fileSystem = FileSystemRule()

    @Before
    fun setup() {
        every { configHandler.getBaseDir() } returns fileSystem.directory.absolutePath.toString()
    }

    @Test
    fun `When feedback dir empty, exits`() {
        every { feedbackFileHelper.readFeedbackCountFromReviews(any()) } returns mapOf()

        assertThrows<ProgramExitError> {
            saveFeedbackCommand.execute()
        }
    }

    @Test
    fun `When read csv throws exception, exits`() {
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } throws IOException()

        assertThrows<ProgramExitError> {
            saveFeedbackCommand.execute()
        }
    }

    @Test
    fun `When existing file not exists, writes new file with data from review folder`() {
        saveFeedbackCommand.execute()

        assertEquals(reviewsInFolder, feedbackCountWriteCapture.captured)
    }

    @Test
    fun `When write throws exception, exits`() {
        every { feedbackFileHelper.writeFeedbackCountToCsv(any(), capture(feedbackCountWriteCapture)) } throws IOException()

        assertThrows<ProgramExitError> {
            saveFeedbackCommand.execute()
        }
    }

    @Test
    fun `When file exists, writes new file with combined data`() {
        val reviewsInCsv = mapOf(
            "S1" to FeedbackCount(0, 1),
            "S2" to FeedbackCount(0, 1)
        )
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } returns reviewsInCsv
        val expectedFeedbackCount = mapOf(
            "S1" to FeedbackCount(1, 1),
            "S2" to FeedbackCount(0, 2)
        )

        saveFeedbackCommand.execute()

        assertEquals(expectedFeedbackCount, feedbackCountWriteCapture.captured)
    }
}