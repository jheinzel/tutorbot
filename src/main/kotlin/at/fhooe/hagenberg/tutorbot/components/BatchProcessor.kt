package at.fhooe.hagenberg.tutorbot.components

import javax.inject.Inject
import kotlin.Exception
import kotlin.math.max

/** Can be used to print progress while processing a bunch of items. */
class BatchProcessor @Inject constructor() {

    fun <T, R : Any> process(items: List<T>, progressMessage: String, doneMessage: String, block: (T) -> R?): List<R> {
        var length = 0

        // Process items
        val result = items.mapIndexedNotNull { index, item ->
            length = max(length, printProgress("$progressMessage (${index + 1}/${items.size})"))
            try {
                block(item)
            } catch (exception: Exception) {
                null // Ignore errors while processing
            }
        }
        printCompletion(doneMessage, length)

        return result
    }

    private fun printProgress(message: String): Int {
        print("\r$message")
        return message.length // Needed to clear the line after processing has finished
    }

    private fun printCompletion(message: String, length: Int) {
        print("\r$message")
        repeat(length - message.length) {
            print(" ") // Clear the rest of the line
        }
        println() // Finish the line
    }
}
