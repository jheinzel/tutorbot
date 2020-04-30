package at.fhooe.hagenberg.tutorbot.network

import at.fhooe.hagenberg.tutorbot.auth.MailAuthenticator
import java.io.File
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

@Singleton
class MailClient @Inject constructor(
    private val authenticator: MailAuthenticator
) {
    private val session by lazy { Session.getInstance(getSettings(), authenticator) }

    fun sendMail(mail: Mail) {
        val message = MimeMessage(session)
        message.setFrom(InternetAddress(mail.from))
        message.setRecipients(Message.RecipientType.TO, mail.to.map(::InternetAddress).toTypedArray())
        message.subject = mail.subject

        // Build the message body with all attachments
        val body = MimeBodyPart().apply { setText(mail.body) }
        val attachment = MimeBodyPart().apply { attachFile(mail.attachment) }
        val multipart = MimeMultipart().apply {
            addBodyPart(body)
            addBodyPart(attachment)
        }
        message.setContent(multipart)

        // Send the email
        Transport.send(message)
    }

    private fun getSettings() = Properties().apply {
        put("mail.smtp.auth", "true")
        put("mail.smtp.starttls.enable", "true")
        put("mail.smtp.host", "smtps.fh-ooe.at")
        put("mail.smtp.port", "587")
        put("mail.smtp.ssl.trust", "smtps.fh-ooe.at")
    }

    data class Mail(val from: String,
                    val to: List<String>,
                    val subject: String,
                    val body: String,
                    val attachment: File)
}
