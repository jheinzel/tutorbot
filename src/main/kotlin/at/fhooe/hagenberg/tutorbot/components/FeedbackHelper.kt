package at.fhooe.hagenberg.tutorbot.components

import java.io.File
import javax.inject.Inject

class FeedbackHelper @Inject constructor() {
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
    fun readAllReviewsFromDir(dir: File): Set<Review> {
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
    fun readFeedbackCountForStudents(feedbackDir: File): Map<String, FeedbackCount> {
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

    companion object {
        const val STUDENT_NR_PATTERN = "[sS][0-9]+"
    }
}