package at.fhooe.hagenberg.tutorbot.domain

/**
 *  Represents a Review, which has a student who submitted the code and one who reviewed it.
 */
data class Review(
    val fileName: String,
    val subStudentNr: String,
    val revStudentNr: String
)