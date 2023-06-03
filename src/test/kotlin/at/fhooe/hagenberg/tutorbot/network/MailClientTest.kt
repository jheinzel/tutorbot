package at.fhooe.hagenberg.tutorbot.network

import at.fhooe.hagenberg.tutorbot.auth.MailAuthenticator
import at.fhooe.hagenberg.tutorbot.components.ConfigHandler
import io.mockk.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import javax.mail.Message
import javax.mail.Transport
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class MailClientTest {
    private val mailServer = "mail.server"
    private val mailPort = "777"

    private val mailAuthenticator = mockk<MailAuthenticator>()

    private val configHandler = mockk<ConfigHandler> {
        every { getEmailServer() } returns mailServer
        every { getEmailPort() } returns mailPort
    }

    private val mailClient = MailClient(mailAuthenticator, configHandler)

    private val mailSlot = slot<MimeMessage>()

    @Before
    fun setup() {
        mockkStatic(Transport::class)
        every { Transport.send(capture(mailSlot)) } just Runs
    }

    @After
    fun teardown() {
        unmockkStatic(Transport::class)
    }

    @Test
    fun `Mail server settings are configured correctly`() {
        val mail = MailClient.Mail("from@mail.com", listOf(), "", "", File(""))
        mailClient.sendMail(mail)
        val session = mailSlot.captured.session

        assertEquals("true", session.properties["mail.smtp.auth"])
        assertEquals("true", session.properties["mail.smtp.starttls.enable"])
        assertEquals(mailServer, session.properties["mail.smtp.host"])
        assertEquals(mailPort, session.properties["mail.smtp.port"])
        assertEquals(mailServer, session.properties["mail.smtp.ssl.trust"])
    }

    @Test
    fun `Mails get sent correctly`() {
        val attachment = File(ClassLoader.getSystemResource("websites/Blank.html").toURI())
        val mail = MailClient.Mail("from@mail.com", listOf("a@mail.com", "b@mail.net"), "Subject", "Body", attachment)

        mailClient.sendMail(mail)
        val message = mailSlot.captured

        val recipients = message.getRecipients(Message.RecipientType.TO).map { recipient -> recipient.toString() }
        assertEquals(listOf("a@mail.com", "b@mail.net"), recipients)
        assertEquals("from@mail.com", message.from[0].toString())
        assertEquals("Subject", message.subject)

        val multipart = message.content as MimeMultipart
        assertEquals("Body", multipart.getBodyPart(0).content)
        assertEquals("Blank.html", multipart.getBodyPart(1).fileName)
    }
}
