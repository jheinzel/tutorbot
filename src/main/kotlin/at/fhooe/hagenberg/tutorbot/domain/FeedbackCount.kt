package at.fhooe.hagenberg.tutorbot.domain

/**
 * Tracks amount of feedbacks a student has received on submissions and reviews. Ordered by submission first then review.
 */
data class FeedbackCount(val submission: Int, val review: Int) : Comparable<FeedbackCount> {
    override fun compareTo(other: FeedbackCount): Int {
        return compareValuesBy(this, other, { it.submission }, { it.review })
    }
}