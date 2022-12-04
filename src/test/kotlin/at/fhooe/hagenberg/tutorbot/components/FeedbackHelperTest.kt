package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.components.FeedbackHelper.FeedbackCount
import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import at.fhooe.hagenberg.tutorbot.testutil.rules.FileSystemRule
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class FeedbackHelperTest : CommandLineTest() {
    private val lowerCaseStudentNumbers = listOf("s1", "s2", "s3", "s4", "s2210101010")
    private val studentFeedbackCount = listOf(
        "s1" to FeedbackCount(2, 0),
        "s2" to FeedbackCount(0, 2),
        "s3" to FeedbackCount(1, 0),
        "s4" to FeedbackCount(1, 1),
        "s2210101010" to FeedbackCount(0, 1)
    )

    private val configHandler = mockk<ConfigHandler> {
        // Ex. and review dir is same as base
        every { getExerciseSubDir() } returns "."
        every { getReviewsSubDir() } returns "."
        val pdfsLoc = ClassLoader.getSystemResource("pdfs").toString().split("file:/").last()
        every { getFeedbackDir() } returns pdfsLoc
        every { getBaseDir() } returns pdfsLoc
    }

    private val feedbackHelper = FeedbackHelper(configHandler)

    @get:Rule
    val fileSystem = FileSystemRule()

    @Test
    fun `Read reviews ignores invalid files`() {
        val res = feedbackHelper.readReviewsForExercise()
        val invalidFile1 = "review.pdf"
        val invalidFile2 = "s1-invalid-file.pdf"

        assert(res.none { r -> r.fileName.contains(invalidFile1) || r.fileName.contains(invalidFile2) })
    }

    @Test
    fun `Read reviews returns empty when dir empty`() {
        every { configHandler.getBaseDir() } returns fileSystem.directory.absolutePath
        val res = feedbackHelper.readReviewsForExercise()

        assert(res.isEmpty())
    }

    @Test
    fun `Read reviews includes right amount of files`() {
        val res = feedbackHelper.readReviewsForExercise()

        assert(res.size == 4)
    }

    @Test
    fun `Read reviews gets student numbers lower case`() {
        val res = feedbackHelper.readReviewsForExercise()

        assert(res.all { r -> r.revStudentNr in lowerCaseStudentNumbers && r.subStudentNr in lowerCaseStudentNumbers })
    }

    @Test
    fun `Read feedback returns empty when dir empty`() {
        every { configHandler.getFeedbackDir() } returns fileSystem.directory.absolutePath
        val res = feedbackHelper.readFeedbackCountForStudents()

        assert(res.isEmpty())
    }

    @Test
    fun `Read feedback has key for every lower case student number`() {
        val res = feedbackHelper.readFeedbackCountForStudents()

        assert(res.all { f -> f.key in lowerCaseStudentNumbers })
    }

    @Test
    fun `Read feedback FeedbackCount has correct amount of submissions and reviews for each student`() {
        val res = feedbackHelper.readFeedbackCountForStudents()

        assert(studentFeedbackCount.all { s -> res[s.first] == s.second })
    }
}