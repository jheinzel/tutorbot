package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.domain.FeedbackCount
import at.fhooe.hagenberg.tutorbot.domain.Review
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackFileHelper @Inject constructor() {

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
     * Gathers the amount of feedbacks the students have received from a directory. Student number keys normalized to lower case.
     */
    fun readFeedbackCountFromReviews(feedbackDir: File): Map<String, FeedbackCount> {
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
     * Reads the persisted feedback counts for each student from a CSV file if possible.
     * Student numbers are normalized to lower case. Format: student,submissions,reviews
     */
    fun readFeedbackCountFromCsv(csvFile: File): Map<String, FeedbackCount> {
        csvFile.bufferedReader().use {
            if (it.readLine() != CSV_HEADER) throw IllegalArgumentException("Invalid CSV file, header '$CSV_HEADER' missing.")
            return it.lineSequence()
                .filter { line -> line.isNotBlank() }
                .map { line ->
                    val (student, submission, review) = line.split(',', limit = 3)
                    student.toLowerCase() to FeedbackCount(submission.toInt(), review.toInt())
                }.toMap()
        }
    }

    /**
     * Writes the feedback count to a CSV file.
     * Student numbers are normalized to lower case. Format: student,submissions,reviews
     */
    fun writeFeedbackCountToCsv(csvFile: File, feedbackCount: Map<String, FeedbackCount>) {
        csvFile.bufferedWriter().use {
            it.write(CSV_HEADER)
            it.newLine()
            feedbackCount.forEach { (s, f) ->
                it.write("${s.toLowerCase()},${f.submission},${f.review}")
                it.newLine()
            }
        }
    }

    companion object {
        const val STUDENT_NR_PATTERN = "[sS][0-9]+"
        const val CSV_HEADER = "student,submissions,reviews"
    }
}