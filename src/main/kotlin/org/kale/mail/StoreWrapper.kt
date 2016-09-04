package org.kale.mail

import com.sun.mail.imap.IMAPFolder
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
class StoreWrapper(val account: EmailAccountConfig,
                   val store: IMAPStore = createStore("imaps"),  // default to ssl connection
                   val dryRun: Boolean = false
                  ) {
    //
    // Public
    //

    val expunge = (dryRun == false)

    fun scanFolder(folderName: String, callback: MailCallback, startUID: Long = -1, doFirstRead: Boolean = true): Unit {
        checkStore()
        ImapFolderScanner(this, folderName, callback, startUID, doFirstRead).startScanning()
    }

    fun getMessages(folderName: String, limit: Int = 0): List<MessageHelper>
    {
        return getMessages(folderName, { folder ->
            val start = if (limit <= 0 || limit >= folder.getMessageCount()) 1 else folder.getMessageCount() - limit + 1
            val count = folder.getMessageCount()

            folder.getMessages(start, count)
        })
    }

    fun getMessagesAfterUID(folderName: String, start: Long) =
        getMessagesByUIDRange(folderName, start + 1)

    fun getMessagesByUIDRange(folderName: String, startUID: Long,
                            endUID: Long = UIDFolder.LASTUID): List<MessageHelper>
    {
        return getMessages(folderName, {folder ->
            if (folder !is IMAPFolder)
                throw imapError(folder)

            folder.getMessagesByUID(startUID, endUID)
        })
    }

    fun getEmailsBeforeDate(folderName: String, date: Instant) =
            getEmailsBeforeDate(folderName, Date.from(date))

    fun getEmailsBeforeDate(folderName: String, date: Date): List<MessageHelper> {

        val olderThan = ReceivedDateTerm(ComparisonTerm.LT, date)

        return getMessages(folderName, { folder ->
            fetch(folder.search(olderThan), folder)
        })

    }

    fun getEmailsAfterDate(folderName: String, instant: Instant) =
            getEmailsAfterDate(folderName,Date.from(instant))

    fun getEmailsAfterDate(folderName: String, date: Date): List<MessageHelper> {

        val newerThan = ReceivedDateTerm(ComparisonTerm.GT, date)
        return getMessages(folderName, { folder ->
            fetch(folder.search(newerThan), folder)
        })
    }

    fun getEmailsByUID(folderName: String, ids: List<Long>): List<MessageHelper> {

        return getMessages(folderName, { folder ->
            if (folder !is IMAPFolder)
                throw imapError(folder)

            folder.getMessagesByUID(ids.toLongArray())

        })
    }

    fun moveTo(toFolderName: String, m: MessageHelper) {

        if (dryRun) {
            logger.info("DRY RUN -- moving message from: ${m.from} subject: ${m.subject} to folder: $toFolderName")
            return
        }

        val fromFolder = getFolder(m.folderName)
        val toFolder = getFolder(toFolderName)
        try {
            if (!toFolder.exists()){
                logger.warn("ignoring request to move message to folder that does not exist: $toFolderName")
                return
            }

            val message = getMessageByUID(fromFolder, m.uid)
            val newMessage = MimeMessage(message)

            newMessage.removeHeader(MoveHeader)
            newMessage.addHeader(MoveHeader, toFolderName)

            val messageArray = arrayOf(newMessage)
            logger.info("moving mail from: ${m.from} subject: ${m.subject} " +
                    "from folder: ${m.folderName} to folder: $toFolderName")
            toFolder.appendMessages(messageArray)
            message.setFlag(Flags.Flag.DELETED, true)
        } catch (e: Exception){
            logger.warn("failed moving message to folder: $toFolderName", e)
        } finally {
            closeFolder(fromFolder)
            closeFolder(toFolder)
        }
    }

    fun delete(permanent: Boolean, m: MessageHelper): Unit {

        if (dryRun) {
            logger.info("DRY RUN -- deleting message from: ${m.from} subject: ${m.subject}")
            return
        }

        if (permanent)
            permanentDelete(m)
        else
            moveTo(MailUtils.trashFolder(account), m)
    }

    fun permanentDelete(m: MessageHelper) {
        val folder = getFolder(m.folderName)
        try {
            val message = getMessageByUID(folder,m.uid)
            message.setFlag(Flags.Flag.DELETED, true)
        } finally {
            closeFolder(folder)
        }
    }

    fun getFolders(): List<FolderWrapper>  {
        checkStore()
        return store.getDefaultFolder().list("*").map{f -> FolderWrapper.create(f, this)}
    }

    fun hasFolder(name: String): Boolean {
        return getFolder(name).exists()
    }

    //
    // Internal
    //

    internal fun getMessageByUID(folder: Folder, uid: Long): MimeMessage {

        if (folder !is IMAPFolder)
            throw imapError(folder)

        return folder.getMessageByUID(uid) as MimeMessage
    }

    internal fun <T>doWithFolder(folderName: String, lambda: (Folder) -> T): T {
        val folder = getFolder(folderName)
        try {
            return lambda(folder)
        } finally {
            closeFolder(folder)
        }
    }

    fun getMessages(folderName: String, lambda: (Folder) -> Array<Message>): List<MessageHelper>
    {
        return doWithFolder(folderName, { folder ->
            val messages = lambda(folder)

            fetch(messages, folder)
            messages.map{MessageHelper.create(it, this)}
        })
    }

    internal fun getFolder(folderName: String): Folder {
        checkStore()
        val folder = store.getFolder(folderName)

        folder.open(getPermission())
        return folder
    }

    internal fun closeFolder(folder: Folder?) {

        if (folder != null && folder.isOpen()) {
            try {
                folder.close(expunge)
            } catch (e: Exception) {
                logger.info(e)
            }
        }

    }


    private fun imapError(folder: Folder): Exception {
        return RuntimeException("folder: ${folder.name} for: ${account.user} does not support UID's")
    }

    private fun fetchMessages(messages: Array<Message>) = messages.map {
        MessageHelper.create(it as MimeMessage, this)}

    private fun getPermission() = if (dryRun) Folder.READ_ONLY else Folder.READ_WRITE



    private fun getEmails(messages: Array<Message>, folder: Folder): List<MessageHelper> {
        fetch(messages, folder)
        return messages.map { m: Message ->  MessageHelper.create(m as MimeMessage, this) }
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

    companion object {
        val logger = LogManager.getLogger(StoreWrapper::class.java.name)
        val defaultFetchProfile = createDefaultFetchProfile()
        val MoveHeader = "Mailscript-Move"

        private fun fetch(messages: Array<Message>, folder: Folder): Array<Message> {

            logger.debug("fetching ${messages.count()} email(s) from ${folder.name}")
            folder.fetch(messages, defaultFetchProfile)
            logger.debug("finishing fetch for ${folder.getName()}")

            return messages
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
            fp.add(IMAPFolder.FetchProfileItem.HEADERS) //load all headers
            return fp
        }
    }
}

