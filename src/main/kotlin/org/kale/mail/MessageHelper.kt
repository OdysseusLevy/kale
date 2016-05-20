package org.kale.mail

import com.sun.mail.imap.IMAPMessage
import javax.mail.Address
import javax.mail.Flags
import javax.mail.Message

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
class MessageHelper(val message: Message) {
    init {

        when(message) {
            is IMAPMessage -> message.setPeek(true)
        }
    }

    companion object {
        val none = arrayOf<Address>()
    }

    //
    // Non lazy values. (We prefetch them)
    //

    val subject: String = message.subject ?: ""
    val uid: Long = MailUtils.getUID(message)

    val to = AddressHelper.getFirst(message.getRecipients(Message.RecipientType.TO) ?: none)
    val toAll = AddressHelper.getAll(message.getRecipients(Message.RecipientType.TO) ?: none)
    val ccAll = AddressHelper.getAll(message.getRecipients(Message.RecipientType.CC) ?: none)
    val bccAll = AddressHelper.getAll(message.getRecipients(Message.RecipientType.BCC)?: none)

    val replyTo = AddressHelper.getFirst(message.getReplyTo() ?: none)
    val replyToAll = AddressHelper.getAll(message.getReplyTo() ?: none)

    val from = AddressHelper.getFirst(message.getFrom() ?: none)
    val fromAll = AddressHelper.getAll(message.getFrom() ?: none)

    val isRead = message.isSet(Flags.Flag.SEEN)
    val size = message.getSize()

    //
    // Lazy values
    //

    // TODO

    // DKIM

    //    lazy val dkimResult = DkimVerifier.verify(this.message)
//    lazy val dkimHeader = DkimVerifier.verifyHeaders(this.message)
//    lazy val verifiedHost = if (dkimHeader.isDefined) dkimHeader.get.domain else ""

//
//    lazy val folder = message.getFolder.getName
//    lazy val body: String = MimePart.getBodyText(message).getOrElse("")

//    lazy val headers = MailMessageHelper.getHeaders(message)

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
//    //
//    // Time stuff
//    //
//
//    lazy val received = message.getReceivedDate
//    lazy val messageAge = Duration.between(received.toInstant, Instant.now())
//
//    lazy val weeksAgo: Long = messageAge.toDays / 7
//    lazy val daysAgo: Long =  messageAge.toDays
//    lazy val hoursAgo: Long = messageAge.toHours
//
//    //
//    // Headers
//    //
//
//    def hasHeader(name: String): Boolean = headers.contains(name.toLowerCase)
//    def getHeader(name: String): Option[String] = headers.get(name.toLowerCase)
//    def sentTo(email: String): Boolean = {
//        val emailLower = email.toLowerCase
//        to.exists{ _.getEmail == emailLower}
//    }
//
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