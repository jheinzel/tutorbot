package at.fhooe.hagenberg.tutorbot.commands

import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import at.fhooe.hagenberg.tutorbot.components.FeedbackChooseLogic
import at.fhooe.hagenberg.tutorbot.components.FeedbackFileHelper
import at.fhooe.hagenberg.tutorbot.domain.FeedbackCount
import at.fhooe.hagenberg.tutorbot.domain.Review
import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.assertThrows
import at.fhooe.hagenberg.tutorbot.testutil.getResource
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import at.fhooe.hagenberg.tutorbot.util.ProgramExitError
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import kotlin.random.Random

class ChooseFeedbackCommandTest : CommandLineTest() {
    private val reviewSubDir = "reviews"
    private val exerciseSubDir = "ue01"

    private val feedbackFileHelper = mockk<FeedbackFileHelper>()
    private val saveFeedbackCommand = mockk<SaveFeedbackCommand>()
    private val configHandler = mockk<ConfigHandler> {
        every { getExerciseSubDir() } returns exerciseSubDir
        every { getReviewsSubDir() } returns reviewSubDir
    }
    private val random = mockk<Random> {
        every { nextInt(any()) } returns 0
    }
    private val feedbackChooseLogic = FeedbackChooseLogic(random) // The logic is being tested as part of the command

    private val chooseFeedbackCmd =
        ChooseFeedbackCommand(configHandler, feedbackFileHelper, feedbackChooseLogic, saveFeedbackCommand)

    @get:Rule
    val fileSystem = FileSystemRule()

    private var reviewLocation: String? = null

    @Before
    fun setup() {
        every { configHandler.getBaseDir() } returns fileSystem.directory.absolutePath
        reviewLocation =
            fileSystem.directory.resolve(exerciseSubDir).resolve(reviewSubDir).toString()
    }

    private fun verifyExpectedFiles(fileNames: List<String>) {
        val filesInFolder = File(reviewLocation!!).listFiles()!!.map { it.name }
        assert(fileNames.all { f -> f in filesInFolder })
    }

    private fun verifyMovedFiles(fileNames: List<String>) {
        val filesInFolder = File(reviewLocation!!, ChooseFeedbackCommand.NOT_SELECTED_DIR).listFiles()!!.map { it.name }
        assert(fileNames.all { f -> f in filesInFolder })
    }

    private fun setupTestFiles() {
        every { feedbackFileHelper.readAllReviewsFromDir(any()) } returns setOf(
            Review("S3-S4_S3-S4.pdf", "s3", "s4"),
            Review("S4-S2210101010_S4_TestName.pdf", "s4", "s2210101010")
        )

        getResource("pdfs/S3-S4_S3-S4.pdf").copyTo(File(reviewLocation, "S3-S4_S3-S4.pdf"))
        getResource("pdfs/S4-S2210101010_S4_TestName.pdf").copyTo(
            File(reviewLocation, "S4-S2210101010_S4_TestName.pdf")
        )
    }

    @Test
    fun `Exit when review dir is empty`() {
        every { feedbackFileHelper.readAllReviewsFromDir(any()) } returns setOf()

        assertThrows<ProgramExitError> {
            chooseFeedbackCmd.execute()
        }
    }

    @Test
    fun `Exit when feedbackCount is smaller than 1`() {
        every { feedbackFileHelper.readAllReviewsFromDir(any()) } returns
                setOf(Review("S1-S2_S1-S2.pdf", "s1", "s2"))
        every { configHandler.getFeedbackAmount() } returns 0

        assertThrows<ProgramExitError> {
            chooseFeedbackCmd.execute()
        }
    }

    @Test
    fun `Exit when feedbackRandomCount is greater than feedbackCount`() {
        every { feedbackFileHelper.readAllReviewsFromDir(any()) } returns
                setOf(Review("S1-S2_S1-S2.pdf", "s1", "s2"))
        every { configHandler.getFeedbackAmount() } returns 4
        every { configHandler.getFeedbackRandomAmount() } returns 5

        assertThrows<ProgramExitError> {
            chooseFeedbackCmd.execute()
        }
    }

    @Test
    fun `Exit when feedbackRandomCount smaller than 0`() {
        every { feedbackFileHelper.readAllReviewsFromDir(any()) } returns
                setOf(Review("S1-S2_S1-S2.pdf", "s1", "s2"))
        every { configHandler.getFeedbackAmount() } returns 4
        every { configHandler.getFeedbackRandomAmount() } returns -1

        assertThrows<ProgramExitError> {
            chooseFeedbackCmd.execute()
        }
    }

    @Test
    fun `With no feedback csv, the only review is chosen`() {
        every { feedbackFileHelper.readAllReviewsFromDir(any()) } returns
                setOf(Review("S1-S2_S1-S2.pdf", "s1", "s2"))
        getResource("pdfs/S1-S2_S1-S2.pdf").copyTo(
            File(reviewLocation, "S1-S2_S1-S2.pdf")
        )
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } throws FileNotFoundException()
        every { configHandler.getFeedbackAmount() } returns 1
        every { configHandler.getFeedbackRandomAmount() } returns 0

        chooseFeedbackCmd.execute()

        verifyExpectedFiles(listOf("S1-S2_S1-S2.pdf"))
    }

    @Test
    fun `Exits when read feedback count throws IOException`() {
        every { feedbackFileHelper.readAllReviewsFromDir(any()) } returns
                setOf(Review("S1-S2_S1-S2.pdf", "s1", "s2"))
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } throws IOException()
        every { configHandler.getFeedbackAmount() } returns 1
        every { configHandler.getFeedbackRandomAmount() } returns 0

        assertThrows<ProgramExitError> {
            chooseFeedbackCmd.execute()
        }
    }

    @Test
    fun `Same student cannot be selected for two reviews, prefer student with no feedbacks`() {
        setupTestFiles()

        // S4 has feedback, S3 does not, prefer S3
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } returns mapOf("s4" to FeedbackCount(1, 0))
        every { configHandler.getFeedbackAmount() } returns 2
        every { configHandler.getFeedbackRandomAmount() } returns 0

        chooseFeedbackCmd.execute()

        verifyExpectedFiles(listOf("S3-S4_S3-S4.pdf"))
        verifyMovedFiles(listOf("S4-S2210101010_S4_TestName.pdf"))
    }

    @Test
    fun `Same student cannot be selected for two reviews, prefer student with fewer feedbacks`() {
        // S4 has more reviews, so S3 is picked
        setupTestFiles()
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } returns mapOf(
            "s3" to FeedbackCount(1, 1),
            "s4" to FeedbackCount(2, 1)
        )
        every { configHandler.getFeedbackAmount() } returns 2
        every { configHandler.getFeedbackRandomAmount() } returns 0

        chooseFeedbackCmd.execute()

        verifyExpectedFiles(listOf("S3-S4_S3-S4.pdf"))
        verifyMovedFiles(listOf("S4-S2210101010_S4_TestName.pdf"))
    }

    @Test
    fun `Random student is selected with same feedback count`() {
        // Both have same amount of reviews, S4 is picked randomly
        setupTestFiles()
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } returns mapOf(
            "s3" to FeedbackCount(1, 1),
            "s4" to FeedbackCount(1, 1)
        )
        every { random.nextInt(any()) } returns 1 // Take second item which is submission of s4
        every { configHandler.getFeedbackAmount() } returns 1
        every { configHandler.getFeedbackRandomAmount() } returns 0

        chooseFeedbackCmd.execute()

        verify(exactly = 1) { random.nextInt(any()) }
        confirmVerified(random)
        verifyMovedFiles(listOf("S4-S2210101010_S4_TestName.pdf"))
        verifyExpectedFiles(listOf("S3-S4_S3-S4.pdf"))
    }

    @Test
    fun `Random student is selected regardless of feedback count`() {
        // S4 has more reviews, but is picked because of randomCount
        setupTestFiles()
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } returns mapOf(
            "s3" to FeedbackCount(1, 1),
            "s4" to FeedbackCount(2, 1)
        )
        every { random.nextInt(any()) } returns 1 // Take second item which is submission of s4
        every { configHandler.getFeedbackAmount() } returns 1
        every { configHandler.getFeedbackRandomAmount() } returns 1

        chooseFeedbackCmd.execute()

        verify(exactly = 1) { random.nextInt(any()) }
        confirmVerified(random)
        verifyExpectedFiles(listOf("S4-S2210101010_S4_TestName.pdf"))
        verifyMovedFiles(listOf("S3-S4_S3-S4.pdf"))
    }

    @Test
    fun `Student with fewer reviews than submissions gets selected as reviewer`() {
        // S4 has not gotten any feedbacks on reviews, choose where S4 is reviewer
        setupTestFiles()
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } returns mapOf(
            "s3" to FeedbackCount(1, 1),
            "s4" to FeedbackCount(1, 0)
        )
        every { configHandler.getFeedbackAmount() } returns 1
        every { configHandler.getFeedbackRandomAmount() } returns 0

        chooseFeedbackCmd.execute()

        verifyExpectedFiles(listOf("S3-S4_S3-S4.pdf"))
        verifyMovedFiles(listOf("S4-S2210101010_S4_TestName.pdf"))
    }

    @Test
    fun `When save feedback chosen, calls SaveFeedbackCommand`() {
        // This configuration should lead to a successful execution of ChooseFeedbackCommand to a point where saving is possible
        setupTestFiles()
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } returns mapOf("s4" to FeedbackCount(1, 0))
        every { configHandler.getFeedbackAmount() } returns 2
        every { configHandler.getFeedbackRandomAmount() } returns 0
        systemIn.provideLines("Y")

        chooseFeedbackCmd.execute()

        verify(exactly = 1) { saveFeedbackCommand.execute() }
    }

    @Test
    fun `When save feedback not chosen, does not call SaveFeedbackCommand`() {
        // This configuration should lead to a successful execution of ChooseFeedbackCommand to a point where saving is possible
        setupTestFiles()
        every { feedbackFileHelper.readFeedbackCountFromCsv(any()) } returns mapOf("s4" to FeedbackCount(1, 0))
        every { configHandler.getFeedbackAmount() } returns 2
        every { configHandler.getFeedbackRandomAmount() } returns 0
        systemIn.provideLines("N")

        chooseFeedbackCmd.execute()

        confirmVerified(saveFeedbackCommand)
    }
}