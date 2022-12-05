package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.util.exitWithError
import at.fhooe.hagenberg.tutorbot.util.promptTextInput
import java.io.File
import java.nio.file.Path
import javax.inject.Inject

class FeedbackHelper @Inject constructor(
    private val configHandler: ConfigHandler
) {
    // Tracks amount of feedbacks a student has received on submissions and reviews. Ordered by submission first then review.
    data class FeedbackCount(val submission: Int, val review: Int) : Comparable<FeedbackCount> {
        override fun compareTo(other: FeedbackCount): Int {
            return compareValuesBy(this, other, { it.submission }, { it.review })
        }
    }

    // Represents a Review, which has a student who submitted the code and one who reviewed it.
    data class Review(
        val fileName: String,
        val subStudentNr: String,
        val revStudentNr: String
    )

    /**
     * Reads all review files from a directory matching with the STUDENT_NR_PATTERN ignoring other files.
     * Student numbers are normalized to lower case.
     */
    private fun readAllReviewsFromDir(dir: File): Set<Review> {
        val studentNrRegex = STUDENT_NR_PATTERN.toRegex()
        return dir.listFiles()?.mapNotNull { f ->
            // Match first student number with submitter, second as reviewer
            studentNrRegex.find(f.name)?.let { firstMatch ->
                val submitter = firstMatch.value
                firstMatch.next()?.let { secondMatch ->
                    val reviewer = secondMatch.value
                    Review(f.name, submitter.toLowerCase(), reviewer.toLowerCase())
                }
            }
        }?.toSet() ?: setOf()
    }

    /**
     * Gathers the amount of feedbacks the students have received. Student number keys normalized to lower case.
     */
    fun readFeedbackCountForStudents(): Map<String, FeedbackCount> {
        val dirInput = configHandler.getFeedbackDir()
            ?: promptTextInput("Enter directory with previous feedbacks (relative or absolute path):")
        val feedbackDir = Path.of(dirInput).toFile()

        if (!feedbackDir.isDirectory) {
            exitWithError("Location $feedbackDir does not point to a valid directory.")
        }

        val reviews = readAllReviewsFromDir(feedbackDir)
        val countMap = mutableMapOf<String, FeedbackCount>()
        // Each occurrence as submitter increases the FeedbackCount.submissions, same for reviewer
        // Respecting immutability of data class using copy
        reviews.forEach { r ->
            countMap[r.subStudentNr] = countMap[r.subStudentNr]?.let { it.copy(submission = it.submission + 1) }
                ?: FeedbackCount(1, 0)
            countMap[r.revStudentNr] = countMap[r.revStudentNr]?.let { it.copy(review = it.review + 1) }
                ?: FeedbackCount(0, 1)
        }

        return countMap
    }

    /**
     * Gets all available reviews from an exercise. Student numbers normalized to lower case.
     */
    fun readReviewsForExercise(): Set<Review> {
        val baseDir = configHandler.getBaseDir() ?: promptTextInput("Enter base directory:")
        val exerciseSubDir = configHandler.getExerciseSubDir() ?: promptTextInput("Enter exercise subdirectory:")
        val reviewsDir = configHandler.getReviewsSubDir() ?: promptTextInput("Enter reviews subdirectory:")
        val targetDirectory = Path.of(baseDir, exerciseSubDir, reviewsDir).toFile()

        return readAllReviewsFromDir(targetDirectory)
    }

    companion object {
        const val STUDENT_NR_PATTERN = "[sS][0-9]+"
    }
}