package org.kale.mail

import com.sun.mail.imap.IMAPMessage
import org.kale.dkim.DkimResult
import org.kale.dkim.DkimVerifier
import org.kale.dkim.DnsHelper
import java.time.Duration
import java.time.Instant
import javax.mail.*
import javax.mail.internet.MimeMessage

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
class MessageHelper(val message: MimeMessage) {
    init {

        when (message) {
            is IMAPMessage -> message.setPeek(true)
        }
    }

    companion object {
        val none = arrayOf<Address>()

        fun getHeaders(message: Part): Map<String, String> {

            val headers = mutableMapOf<String,String>()

            message.getAllHeaders().toList().forEach { header ->
                if (header is Header)
                    headers.put(header.name.toLowerCase(), header.value)
            }

            return headers
        }
    }

    //
    // Non lazy values. (We prefetch them)
    //

    val subject: String = message.subject ?: ""
    val uid: Long = MailUtils.getUID(message)

    val to = AddressHelper.getFirst(message.getRecipients(Message.RecipientType.TO) ?: none)
    val toAll = AddressHelper.getAll(message.getRecipients(Message.RecipientType.TO) ?: none)
    val ccAll = AddressHelper.getAll(message.getRecipients(Message.RecipientType.CC) ?: none)
    val bccAll = AddressHelper.getAll(message.getRecipients(Message.RecipientType.BCC) ?: none)

    val replyTo = AddressHelper.getFirst(message.getReplyTo() ?: none)
    val replyToAll = AddressHelper.getAll(message.getReplyTo() ?: none)

    val from = AddressHelper.getFirst(message.getFrom() ?: none)
    val fromAll = AddressHelper.getAll(message.getFrom() ?: none)

    val isRead = message.isSet(Flags.Flag.SEEN)
    val size = message.getSize()

    //
    // Lazy values
    //

    //
    // DKIM
    //

    /**
     * Full dkim check on both headers and body.
     * Note that this requires us to fetch the full body, which is much slower than checking just the headers
     */
    val dkimResult: DkimResult by lazy {
        DkimVerifier.verify(this.message, DnsHelper())
    }

    /**
     * Dkim check on the headers. This is usually sufficient to verify if the mail is spam.
     * It does not, however, check the integrity of the body of the text, just the headers.
     */
    val dkimHeader: DkimResult by lazy {
        DkimVerifier.verifyOnlyHeaders(this.message, DnsHelper())
    }

    /**
     * If this email has been properly dkim signed, returns the verified host for this email
     * Otherwise it returns an empty string ""
     */
    val verifiedHost: String by lazy {
        if (dkimHeader.isDefined) dkimHeader.get().domain() else ""
    }

    // TODO

//
//    lazy val folder = message.getFolder.getName
//    lazy val body: String = MimePart.getBodyText(message).getOrElse("")



//    lazy val attachments = MimePart.getAttachments(this.message).toArray
//    lazy val moveHeader: Option[String] = {
//        val result = MailMessageHelper.fetchOneHeader(message, MailUtils.MoveHeader)
//        if (result == null || result.size == 0)
//            None
//        else
//            Option(result(0))
//    }
//
//    def dumpStructure = MimePart.dumpStructure(message)
//
    //
    // Time stuff
    //

    val received: Instant = message.getReceivedDate().toInstant()
    val messageAge: Duration = Duration.between(received, Instant.now())

    val weeksAgo: Long = messageAge.toDays() / 7
    val daysAgo: Long =  messageAge.toDays()
    val hoursAgo: Long = messageAge.toHours()
    val minutesAgo: Long = messageAge.toMinutes()


    //
    // Headers
    //

    val headers: Map<String,String> by lazy {
        MessageHelper.getHeaders(message)
    }

    fun hasHeader(name: String): Boolean = headers.contains(name.toLowerCase())

    fun getHeader(name: String): String? = headers.get(name.toLowerCase())

    fun wasSentTo(email: String): Boolean {
        val emailLower = email.toLowerCase()
        return toAll.firstOrNull{ it.email == emailLower} != null
    }

//    //
//    // Commands
//    //
//
//    def moveTo(folderName: String) {
//        MailUtils.moveTo(folderName, this)
//    }
//
//    def getBytes : ByteArrayOutputStream = {
//        val os =  new ByteArrayOutputStream()
//        val bis = message.writeTo(os)
//        os
//    }
//
//    def delete() {
//        MailUtils.delete(false, this)
//    }
//
//    def delete(permanent: Boolean): Unit = {
//        MailUtils.delete(permanent, this)
//    }
//
//    def saveToFile(fileName: String): Unit = MailUtils.saveToFile(message, new File(fileName))
}