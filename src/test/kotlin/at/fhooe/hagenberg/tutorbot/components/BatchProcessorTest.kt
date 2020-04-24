package at.fhooe.hagenberg.tutorbot.components

import at.fhooe.hagenberg.tutorbot.testutil.CommandLineTest
import org.junit.Assert.assertEquals
import org.junit.Test

class BatchProcessorTest : CommandLineTest() {
    private val batchProcessor = BatchProcessor()

    @Test
    fun `Progress is printed while processing`() {
        val items = listOf(1, 2, 3)
        batchProcessor.process(items, "Processing", "Done") { /* Nothing to do */ }

        val output = systemOut.logWithNormalizedLineSeparator.split("\r")
        assertEquals("Processing (1/3)", output[1])
        assertEquals("Processing (2/3)", output[2])
        assertEquals("Processing (3/3)", output[3])
        assertEquals("Done            \n", output[4])
    }

    @Test
    fun `Null results are ignored during processing`() {
        val items = listOf(1, null, 2)
        val result = batchProcessor.process(items, "", "") { item ->
            item?.plus(2)
        }
        assertEquals(listOf(3, 4), result)
    }

    @Test
    fun `Processing errors are ignored`() {
        val items = listOf(1, 2, 3)
        val result = batchProcessor.process(items, "", "") { item ->
            throw Exception("Can't process item $item")
        }
        assertEquals(0, result.size)
    }
}
