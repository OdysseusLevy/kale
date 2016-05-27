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
class MessageHelper(
        val store: StoreWrapper,
        val subject: String,
        val uid: Long,
        val to: AddressHelper,
        val toAll: List<AddressHelper>,
        val ccAll: List<AddressHelper>,
        val bccAll: List<AddressHelper>,

        val replyTo: AddressHelper,
        val replyToAll: List<AddressHelper>,
        val from: AddressHelper,
        val fromAll: List<AddressHelper>,

        val isRead: Boolean,
        val size: Int,
        val folderName: String,
        val received: Instant,
        val dkimHeader: DkimResult,        // DKIM done on just the headers, enough to usually tell if something is spam
        val headers: Map<String,String>

) {
    companion object {

        fun create(message: MimeMessage, store: StoreWrapper): MessageHelper {
            when (message) {
                is IMAPMessage -> message.setPeek(true)
            }

            return MessageHelper(
                store= store,
                subject = message.subject ?: "",
                uid = MailUtils.getUID(message),

                to = AddressHelper.getFirst(message.getRecipients(Message.RecipientType.TO) ?: none),
                toAll = AddressHelper.getAll(message.getRecipients(Message.RecipientType.TO) ?: none),
                ccAll = AddressHelper.getAll(message.getRecipients(Message.RecipientType.CC) ?: none),
                bccAll = AddressHelper.getAll(message.getRecipients(Message.RecipientType.BCC) ?: none),

                replyTo = AddressHelper.getFirst(message.getReplyTo() ?: none),
                replyToAll = AddressHelper.getAll(message.getReplyTo() ?: none),

                from = AddressHelper.getFirst(message.getFrom() ?: none),
                fromAll = AddressHelper.getAll(message.getFrom() ?: none),

                isRead = message.isSet(Flags.Flag.SEEN),
                size = message.getSize(),

                folderName = message.folder.name,
                received = message.getReceivedDate().toInstant(),
                dkimHeader = DkimVerifier.verifyOnlyHeaders(message, DnsHelper()),
                headers = MessageHelper.getHeaders(message)

            )
        }

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
    // Time stuff
    //


    val messageAge: Duration = Duration.between(received, Instant.now())

    val weeksAgo: Long = messageAge.toDays() / 7
    val daysAgo: Long =  messageAge.toDays()
    val hoursAgo: Long = messageAge.toHours()
    val minutesAgo: Long = messageAge.toMinutes()

    //
    // Lazy values
    //

    //
    // DKIM
    //

    /**
     * If this email has been properly dkim signed, returns the verified host for this email.
     * Otherwise it returns an empty string ""
     */
    val verifiedHost: String = if (dkimHeader.isDefined) dkimHeader.get().domain() else ""


    //
    // Body
    //

    /**
     * Full dkim check on both headers and body.
     * Note that this requires us to fetch the full body, which is much slower than checking just the headers
     */
    val dkimResult: DkimResult by lazy { DkimVerifier.verify(message, DnsHelper()) }

    val body: String by lazy { MimePart.getBodyText(message) }



    //
    // Headers
    //

    val moveHeader = getHeader(StoreWrapper.MoveHeader)

    fun hasHeader(name: String): Boolean = headers.contains(name.toLowerCase())

    fun getHeader(name: String): String = headers.get(name.toLowerCase()) ?: ""

    fun wasSentTo(email: String): Boolean {
        val emailLower = email.toLowerCase()
        return toAll.firstOrNull{ it.email == emailLower} != null
    }

    //
    // Commands
    //

    fun  moveTo(folderName: String) {
        store.moveTo(folderName, this)
    }


    fun  delete() {
        store.delete(false, this)
    }

    fun delete(permanent: Boolean) {
        store.delete(permanent, this)
    }

    fun dumpStructure() {
        MimePart.dumpStructure(message)
    }

    //
    // TODO
    //

//
//    lazy val folder = message.getFolder.getName
//    def getBytes : ByteArrayOutputStream = {
//        val os =  new ByteArrayOutputStream()
//        val bis = message.writeTo(os)
//        os
//    }
//
//    def saveToFile(fileName: String): Unit = MailUtils.saveToFile(message, new File(fileName))
//    lazy val attachments = MimePart.getAttachments(this.message).toArray
//

//

    //
    // Internal
    //

    /**
     * Loads a message that holds cached body info.
     * We want to only have to load this information once
     */
    internal val message: MimeMessage by lazy { getCachedMessage()}

    internal fun getCachedMessage(): MimeMessage {
        val folder = store.getFolder(folderName)
        try {
            val m  = store.getMessageByUID(folder, uid)
            when (m) {
                is IMAPMessage -> {
                    m.setPeek(true)
                    m.getContentType()  // forces a loadStructure() call
                }
            }
            m.getContent()  // forces a parse() call
            return m
        } finally {
            store.closeFolder(folder)
        }
    }

}