package org.kale.mail
import org.apache.logging.log4j.LogManager
import java.time.Instant
import java.util.*
import javax.mail.Folder
import javax.mail.Message
import javax.mail.internet.MimeMessage
import javax.mail.search.ComparisonTerm
import javax.mail.search.ReceivedDateTerm

/**
 * Simplified Folder
 */
class FolderWrapper(var folder: Folder, val storeWrapper: StoreWrapper) {

    val logger = LogManager.getLogger(FolderWrapper::class.java.name)

    /**
     * Name of this folder
     * eg "Trash"
     */
    val name = folder.name

    /**
     * Fully qualified name, so it includes parents as well.
     * Eg. [Gmail]/Trash
     */
    val fullName = folder.fullName

    /**
     * Is this the type of folder that can contain messages?
     * For example, Google's [GMail] folder returns false
     */
    val canHoldMessages = (folder.getType() and Folder.HOLDS_MESSAGES) != 0

    /**
     * Is this the type of folder than contains folders (and not messages)?
     * For example, Google's [Gmail] folder returns true
     * @return
     */
    val holdsFolders = (folder.getType() and Folder.HOLDS_FOLDERS) != 0

    fun getFolders(): List<FolderWrapper>  {
        return folder.list("*").map{ f -> FolderWrapper(f, storeWrapper) }
    }

    fun getMessageCount(): Int = folder.messageCount

    fun exists() = folder.exists()

    fun getEmails(limit: Int = 0): List<MessageHelper> {

        val start = if (limit <= 0 || limit >= folder.getMessageCount()) 1 else folder.getMessageCount() - limit + 1
        val count = folder.getMessageCount()

        val messages = folder.getMessages(start, count)
        fetch(messages)
        return messages.map{m -> MessageHelper(m as MimeMessage) }
    }

    fun getEmailsReversed(limit: Int =0): List<MessageHelper> =
            getEmails(limit).asReversed()

    fun getEmailsBeforeDate(date: Instant) = getEmailsBeforeDate(Date.from(date))

    fun getEmailsBeforeDate(date: Date): List<MessageHelper> {

        val olderThan = ReceivedDateTerm(ComparisonTerm.LT, date)
        return fetch(folder.search(olderThan))
    }

    fun getEmailsAfterDate(instant: Instant) = getEmailsAfterDate(Date.from(instant))

    fun getEmailsAfterDate(date: Date): List<MessageHelper> {

        val newerThan = ReceivedDateTerm(ComparisonTerm.GT, date)
        return fetch(folder.search(newerThan))
    }

    fun close() {

        if (folder.isOpen()) {
            try {
                folder.close(storeWrapper.expunge)
            } catch (e: Exception) {
                StoreWrapper.logger.info(e)
            }
        }
    }

    //
    // Internal
    //

    private fun checkFolder(): Folder {
        if (storeWrapper.checkStore())
            return storeWrapper.store.getFolder(name)
        else
            return folder
    }


    private fun fetch(messages: Array<Message>): List<MessageHelper> {
        logger.debug("fetching ${messages.count()} email(s) from ${folder.name}")
        folder.fetch(messages, StoreWrapper.defaultFetchProfile)
        logger.debug("finishing fetch for ${folder.getName()}")

        return messages.map {m -> MessageHelper( m as MimeMessage)}
    }
}