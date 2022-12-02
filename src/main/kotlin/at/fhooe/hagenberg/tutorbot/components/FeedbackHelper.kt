package at.fhooe.hagenberg.tutorbot.components

import javax.inject.Inject

class FeedbackHelper @Inject constructor(
    private val configHandler: ConfigHandler
) {
    // Tracks amount of feedbacks a student has received on submissions and reviews.
    data class FeedbackCount(val submission: Int, val review: Int)
    // Represents a Review, which has a student who submitted the code and one who reviewed it.
    data class Review(val fileName: String, val studentSubNr: String, val studentRevNr: String) // TODO calculate sub and rev from fileName

    fun readFeedbackCountFromDir(): Map<String, FeedbackCount> = TODO("Read files from basedir/feedback.dir, sum up occurrences on left side as submitter, right side reviewer.")

    fun readAvailableReviewsFromDir(): Collection<Review> = TODO("Read filenames from basedir/ex../review.dir")
}