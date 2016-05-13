package org.kale.mail

import com.sun.mail.imap.IMAPFolder
import com.sun.mail.imap.IMAPMessage
import com.sun.mail.imap.IMAPStore
import org.apache.logging.log4j.LogManager
import java.util.*
import javax.mail.*


/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
class StoreHelper(val account: EmailAccountConfig,
                  val storeName: String = "imaps",  // default to ssl connection
                  val dryRun: Boolean = false
                  ) {

    val store: IMAPStore = createStore(storeName)

    companion object {
        val logger = LogManager.getLogger(StoreHelper::class.java.name)
        val defaultFetchProfile = createDefaultFetchProfile()
        val MoveHeader = "Mailscript-Move"

        private fun fetch(messages: Array<Message>, folder: IMAPFolder): Unit {

            logger.debug("fetching ${messages.count()} email(s) from ${folder.name}")
            folder.fetch(messages, defaultFetchProfile)
            logger.debug("finishing fetch for ${folder.getName()}")
        }

        fun createStore(storeName: String): IMAPStore {
            val session: Session = Session.getDefaultInstance(Properties(), null)
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


    public fun getEmails(folderName: String, limit: Int = 0): List<MessageHelper> {
        val folder = getFolder(folderName)

        val start = if (limit <= 0 || limit >= folder.getMessageCount()) 1 else folder.getMessageCount() - limit + 1
        val count = folder.getMessageCount()

        val messages = folder.getMessages(start, count)
        fetch(messages, folder)

        return fetchMessages(messages)
    }

    private fun fetchMessages(messages: Array<Message>) = messages.map { MessageHelper(it as IMAPMessage)}

    private fun getPermission() = if (dryRun) Folder.READ_ONLY else Folder.READ_WRITE

    private fun getFolder(folderName: String): IMAPFolder {
        checkStore()
        val folder = store.getFolder(folderName) as IMAPFolder
        folder.open(getPermission())
        return folder
    }

    private fun getEmails(messages: Array<Message>, folder: IMAPFolder): List<MessageHelper> {
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

