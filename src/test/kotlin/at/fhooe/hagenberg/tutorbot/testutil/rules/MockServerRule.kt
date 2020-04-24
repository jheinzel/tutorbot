package at.fhooe.hagenberg.tutorbot.testutil.rules

import at.fhooe.hagenberg.tutorbot.network.UrlProvider
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class MockServerRule : TestWatcher(), UrlProvider {
    private val mockServer = MockWebServer()

    override fun finished(description: Description?) = mockServer.shutdown()
    override fun baseUrl() = mockServer.url("/").toString()

    fun enqueueResource(name: String) {
        val body = ClassLoader.getSystemResource(name)?.readText() ?: ""
        mockServer.enqueue(MockResponse().setBody(body))
    }

    fun enqueueResponseCode(code: Int) {
        mockServer.enqueue(MockResponse().setResponseCode(code))
    }

    fun start() = mockServer.start()
    fun takeRequest() = mockServer.takeRequest()
}
