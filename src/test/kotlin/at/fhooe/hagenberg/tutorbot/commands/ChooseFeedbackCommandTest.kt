package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper
import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper.Review
import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import at.fhooe.hagenberg.tutorbot.testutil.getResource
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.random.Random

class ChooseFeedbackCommandTest : CommandLineTest() {
    private val reviewLoc = "reviews"
    private val exerciseLoc = "ue01"

    private val configHandler = mockk<ConfigHandler> {
        every { getExerciseSubDir() } returns exerciseLoc
        every { getReviewsSubDir() } returns reviewLoc
    }
    private val feedbackHelper = mockk<FeedbackHelper> {

    }
    private val random = mockk<Random> {
        every { nextInt(any()) } returns 0
    }

    private val chooseFeedbackCmd = ChooseFeedbackCommand(configHandler, feedbackHelper, random)

    @get:Rule
    val fileSystem = FileSystemRule()

    private var reviewLocation: String? = null

    private fun verifyExpectedFiles(fileNames: List<String>) {
        val filesInFolder = File(reviewLocation!!).listFiles()!!.map { it.name }
        assert(fileNames.all { f -> f in filesInFolder })
    }

    private fun verifyMovedFiles(fileNames: List<String>) {
        val filesInFolder = File(reviewLocation!!, ChooseFeedbackCommand.NOT_SELECTED_DIR).listFiles()!!.map { it.name }
        assert(fileNames.all { f -> f in filesInFolder })
    }

    @Before
    fun setup() {
        every { configHandler.getBaseDir() } returns fileSystem.directory.absolutePath.toString()
        reviewLocation =
            Path.of(configHandler.getBaseDir(), configHandler.getExerciseSubDir(), configHandler.getReviewsSubDir())
                .toString()
    }

    @Test
    fun `Exit when review dir is empty`() {
        every { feedbackHelper.readAllReviewsFromDir(any()) } returns setOf()

        assertThrows<ProgramExitError> {
            chooseFeedbackCmd.execute()
        }
    }

    @Test
    fun `Exit when feedbackCount is smaller than 1`() {
        every { feedbackHelper.readAllReviewsFromDir(any()) } returns
                setOf(Review("S1-S2_S1-S2.pdf", "s1", "s2"))
        every { configHandler.getFeedbackAmount() } returns 0

        assertThrows<ProgramExitError> {
            chooseFeedbackCmd.execute()
        }
    }

    @Test
    fun `Exit when feedbackRandomCount is greater than feedbackCount`() {
        every { feedbackHelper.readAllReviewsFromDir(any()) } returns
                setOf(Review("S1-S2_S1-S2.pdf", "s1", "s2"))
        every { configHandler.getFeedbackAmount() } returns 4
        every { configHandler.getFeedbackRandomAmount() } returns 5

        assertThrows<ProgramExitError> {
            chooseFeedbackCmd.execute()
        }
    }

    @Test
    fun `Exit when feedbackRandomCount smaller than 0`() {
        every { feedbackHelper.readAllReviewsFromDir(any()) } returns
                setOf(Review("S1-S2_S1-S2.pdf", "s1", "s2"))
        every { configHandler.getFeedbackAmount() } returns 4
        every { configHandler.getFeedbackRandomAmount() } returns -1

        assertThrows<ProgramExitError> {
            chooseFeedbackCmd.execute()
        }
    }

    @Test
    fun `With no feedback csv, the only review is chosen`() {
        every { feedbackHelper.readAllReviewsFromDir(any()) } returns
                setOf(Review("S1-S2_S1-S2.pdf", "s1", "s2"))
        File(ClassLoader.getSystemResource("pdfs/S1-S2_S1-S2.pdf").toURI()).copyTo(File(reviewLocation, "S1-S2_S1-S2.pdf"))
        every { configHandler.getFeedbackAmount() } returns 1
        every { configHandler.getFeedbackRandomAmount() } returns 0
        every {feedbackHelper.readFeedbackCountFromCsv(any())} throws FileNotFoundException()

        chooseFeedbackCmd.execute()

        verifyExpectedFiles(listOf("S1-S2_S1-S2.pdf"))
    }

    @Test
    fun `Same students cannot be selected for two reviews, prefer those with less feedbacks`() {
        every { feedbackHelper.readAllReviewsFromDir(any()) } returns
                setOf(
                    Review("S3-S4_S3-S4.pdf", "s3", "s4"),
                    Review("S4-S2210101010_S4_TestName.pdf", "s4", "s2210101010")
                )
        getResource("pdfs/S3-S4_S3-S4.pdf").copyTo(File(reviewLocation, "S3-S4_S3-S4.pdf"))
        getResource("pdfs/S4-S2210101010_S4_TestName.pdf").copyTo(File(reviewLocation, "S4-S2210101010_S4_TestName.pdf"))
        // S4 has more feedbacks, prefer S3
        every {feedbackHelper.readFeedbackCountFromCsv(any())} returns mapOf("s4" to FeedbackHelper.FeedbackCount(1, 0))
        every { configHandler.getFeedbackAmount() } returns 2
        every { configHandler.getFeedbackRandomAmount() } returns 0

        chooseFeedbackCmd.execute()

        verifyExpectedFiles(listOf("S3-S4_S3-S4.pdf"))
        verifyMovedFiles(listOf("S4-S2210101010_S4_TestName.pdf"))
    }
}