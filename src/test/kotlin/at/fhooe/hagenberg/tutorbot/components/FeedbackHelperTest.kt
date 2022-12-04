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
    private val lowerCaseStudentNumbers = listOf("s1", "s2", "s3", "s4", "s2210101010")

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
    fun `Read feedback gets student numbers lower case`() {
        val res = feedbackHelper.readReviewsForExercise()

        assert(res.all { r -> r.revStudentNr in lowerCaseStudentNumbers && r.subStudentNr in lowerCaseStudentNumbers})
    }

    @Test
    fun `Read feedback has entry for each student`() {
        every { configHandler.getFeedbackDir() } returns fileSystem.directory.absolutePath
        val res = feedbackHelper.readFeedbackCountForStudents()

        assert(res.isEmpty())
    }
}