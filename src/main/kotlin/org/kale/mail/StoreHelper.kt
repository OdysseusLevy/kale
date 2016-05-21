package org.kale.mail

import com.sun.mail.imap.IMAPFolder
import com.sun.mail.imap.IMAPMessage
import com.sun.mail.imap.IMAPStore
import org.apache.logging.log4j.LogManager
import java.time.Instant
import java.util.*
import javax.mail.*
import javax.mail.internet.MimeMessage
import javax.mail.search.ComparisonTerm
import javax.mail.search.ReceivedDateTerm


/**
 *
 */
class StoreHelper(val account: EmailAccountConfig,
                  val store: IMAPStore = createStore("imaps"),  // default to ssl connection
                  val dryRun: Boolean = false
                  ) {


    companion object {
        val logger = LogManager.getLogger(StoreHelper::class.java.name)
        val defaultFetchProfile = createDefaultFetchProfile()
        val MoveHeader = "Mailscript-Move"

        private fun fetch(messages: Array<Message>, folder: Folder): Unit {

            logger.debug("fetching ${messages.count()} email(s) from ${folder.name}")
            folder.fetch(messages, defaultFetchProfile)
            logger.debug("finishing fetch for ${folder.getName()}")
        }

        fun createStore(storeName: String,
                        session: Session = Session.getDefaultInstance(Properties(), null)): IMAPStore {
            return session.getStore(storeName) as IMAPStore
        }

        fun createDefaultFetchProfile(): FetchProfile {
            val fp = FetchProfile()
            fp.add(FetchProfile.Item.ENVELOPE)
            fp.add(FetchProfile.Item.FLAGS)
            fp.add(FetchProfile.Item.SIZE)
            fp.add(UIDFolder.FetchProfileItem.UID)
            fp.add(MoveHeader)
            return fp
        }
    }

    //
    // Public
    //

    fun scanFolder(folderName: String, callback: ProcessCallback, startUID: Long = -1, doFirstRead: Boolean = true): Unit {
        checkStore()
        ImapFolderScanner(this, folderName, callback, startUID, doFirstRead).startScanning()
    }

    fun getEmails(folderName: String, limit: Int = 0): List<MessageHelper> {
        val folder = getFolder(folderName)

        val start = if (limit <= 0 || limit >= folder.getMessageCount()) 1 else folder.getMessageCount() - limit + 1
        val count = folder.getMessageCount()

        val messages = folder.getMessages(start, count)
        fetch(messages, folder)

        return fetchMessages(messages)
    }

    fun getEmailsReversed(folderName: String, limit: Int =0): List<MessageHelper> =
            getEmails(folderName, limit).asReversed()

    fun getEmailsBeforeDate(folderName: String, date: Instant) = getEmailsBeforeDate(folderName, Date.from(date))

    fun getEmailsBeforeDate(folderName: String, date: Date): List<MessageHelper> {

        val folder = getFolder(folderName)
        val olderThan = ReceivedDateTerm(ComparisonTerm.LT, date)

        val emails = folder.search(olderThan)

        return getEmails(emails, folder)
    }

    fun getEmailsAfterDate(folderName: String, instant: Instant) = getEmailsAfterDate(folderName, Date.from(instant))

    fun getEmailsAfterDate(folderName: String, date: Date): List<MessageHelper> {

        val folder = getFolder(folderName)
        val newerThan = ReceivedDateTerm(ComparisonTerm.GT, date)

        val emails = folder.search(newerThan)

        return getEmails(emails, folder)
    }

    fun moveTo(toFolderName: String, m: MessageHelper) {

        if (dryRun) {
            logger.info("DRY RUN -- moving message from: ${m.from} subject: ${m.subject} to folder: $toFolderName")
            return
        }

        val fromFolder = m.message.folder
        val toFolder = store.getFolder(toFolderName)
        if (!toFolder.exists()){
            logger.warn("ignoring request to move message to folder that does not exist: $toFolderName")
            return
        }

        try {
            toFolder.open(Folder.READ_WRITE)

            val newMessage = MimeMessage(m.message as MimeMessage)

            newMessage.removeHeader(MoveHeader)
            newMessage.addHeader(MoveHeader, toFolderName)

            val messageArray = arrayOf(newMessage)
            logger.info("moving mail from: ${m.from} subject: ${m.subject} to folder: $toFolderName")
            toFolder.appendMessages(messageArray)
            m.message.setFlag(Flags.Flag.DELETED, true)
        } catch (e: Throwable){
            logger.warn("failed moving message to folder: $toFolderName", e)
        }

        closeFolder(toFolder)
    }

    fun delete(permanent: Boolean, m: MessageHelper): Unit {

        if (dryRun) {
            logger.info("DRY RUN -- deleting message from: ${m.from} subject: ${m.subject}")
            return
        }

        if (permanent)
            m.message.setFlag(Flags.Flag.DELETED, true)
        else
            moveTo(MailUtils.trashFolder(account), m)
    }

//
//    def getEmailSafe(folder: IMAPFolder, id: Long): Option[Message] = {
//        Option(folder.getMessageByUID(id))
//    }

//    def getEmailsByUID(folder: IMAPFolder, ids: util.ArrayList[Number]): Array[Email] = {
//        val scalaIds = ids.asScala.toArray
//        getEmailsByUID(folder, scalaIds)
//    }
//
//    def getEmailsByUID(folder: IMAPFolder, ids: Array[Number]): Array[Email] = {
//        val messages = ids.flatMap{id => getEmailSafe(folder, id.longValue())}
//        getEmails(messages, folder)
//    }
//
//    def foreachAfterUID(folderName: String, startUID: Long, callback: Callback): Long = {
//
//        def get(folder: IMAPFolder) = getEmailsAfterUID(folder, startUID)
//        foreach(folderName, get, callback)
//    }

    fun getEmailsAfterUID(folderName: String, startUID: Long): List<MessageHelper> =
            getEmailsAfterUID(getFolder(folderName), startUID)

    fun getEmailsAfterUID(folder: Folder, startUID: Long): List<MessageHelper> {


        if (folder !is IMAPFolder)
            throw imapError(folder)


        val start: Long  =  if (startUID < 0) 0  else startUID
        val messages = folder.getMessagesByUID(start + 1, UIDFolder.LASTUID)

        fetch(messages, folder)

        // Get rid of final message (JavaMail insists on including the message just before our start id)
        return messages.map { MessageHelper(it as MimeMessage)}.filter{it.uid > start}
    }

    fun getFolders(): Array<Folder>  {
        checkStore()
        return store.getDefaultFolder().list("*")
    }

    fun hasFolder(name: String): Boolean {
        return getFolder(name).exists()
    }

    fun getFolder(folderName: String): Folder {
        checkStore()
        val folder = store.getFolder(folderName)

        folder.open(getPermission())
        return folder
    }

    fun closeFolder(folder: Folder?) {

        if (folder != null && folder.isOpen()) {
            try {
                folder.close(dryRun == false)
            } catch (e: Exception) {
                logger.info(e)
            }
        }

    }

    //
    // Internal
    //

    private fun fetchMessages(messages: Array<Message>) = messages.map { MessageHelper(it as MimeMessage)}

    private fun getPermission() = if (dryRun) Folder.READ_ONLY else Folder.READ_WRITE

    private fun imapError(folder: Folder): Exception {
        return RuntimeException("folder: ${folder.name} for: ${account.user} does not support UID's")
    }

    private fun getEmails(messages: Array<Message>, folder: Folder): List<MessageHelper> {
        fetch(messages, folder)
        return messages.map { message: Message -> MessageHelper(message as IMAPMessage) }
    }

    fun checkStore(): Boolean {
        if (!store.isConnected) {
            logger.info("store connecting to account: ${account.imapHost} user: ${account.user}")
            store.connect(account.imapHost, account.user, account.password)
            return true
        } else {
            return false
        }
    }
}

