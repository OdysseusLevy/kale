package org.kale.api

import org.kale.mail.*
import java.time.Instant
import kotlin.comparisons.reverseOrder

/**
 * @author Odysseus Levy (odysseus.levy@am.sony.com)
 */
class EmailAccount(val store: StoreWrapper) {


    companion object {
        val runLambda: (messages: List<MessageHelper>, lambda: MailLambda) -> Unit = { messages, lambda ->
            messages.forEach { message -> lambda.run(Email(message)) }
        }
    }

    class RunHelper(val lambda: MailLambda): MailCallback {
        
        override fun run(latest: List<MessageHelper>) {
            runLambda(latest, lambda)
        }
    }

    /**
     * Continuously scan the folder waiting for new messages to appear.
     * The first time this is run the entire contents of the folder will be scanned. After that the callback will be
     * triggered whenever new messages are added to the folder
     *
     * @param folderName folder to Scan
     * @param scanner callback
     * @group Functions
     */
    fun scanFolder(folderName: String, lambda: MailLambda) {
        store.scanFolder(folderName, RunHelper(lambda))
    }

    /**
     * Scan folder (same as above), but with option to not do the first, initial read
     *
     * @param folderName folder to scan
     * @param doFirstRead if true will first read unread messages
     * @param scanner user supplied callback to handle the emails
     */
    fun scanFolder(folderName: String, doFirstRead: Boolean, lambda: MailLambda) {
        store.scanFolder(folderName, RunHelper(lambda), -1, doFirstRead)
    }

    /**
     * Runs a script against all inbox emails
     * @group Functions
     */
    fun forEach(lambda: MailLambda) {
        forEach("Inbox", lambda)
    }

    /**
     * Runs a script against every email in a given folder
     * order is oldest to newest
     *
     * @param folderName folder to read in
     *  @group Functions
     */
    fun forEach(folderName: String, lambda: MailLambda) {
        forEach(folderName, -1, lambda)
    }

    /**
     * Runs a script against the Inbox with a given limit of how many email to read
     *
     * @param limit returns newest emails up to this limit
     * @param script closure to run against each
     *  @group Functions
     */
    fun forEach(limit: Int, lambda: MailLambda) {
        forEach("Inbox", limit, lambda)
    }

    /**
     * Runs a script against a given folder and limit of how many
     * @param folderName folder to read (eg. Inbox, Junk, etc.)
     * @param limit returns newest emails up to this limit
     * @param script
     *  @group Functions
     */
    fun forEach(folderName: String, limit: Int, lambda: MailLambda) {

        val messages = store.getMessages(folderName, limit)
        runLambda(messages, lambda)
    }

    fun forEachReversed(folderName: String, lambda: MailLambda) =
        forEachReversed(folderName, -1, lambda)
    /**
     * Runs a script against every email in a given folder
     * order is newest to oldest
     *
     * @param folderName
     * @param script
     */
    fun forEachReversed(folderName: String, limit: Int = -1, lambda: MailLambda) {
        val messages = store.getMessages(folderName, limit)
        runLambda(messages, lambda)
    }

    /**
     * Runs a script against all emails before a given date
     *
     * @param folderName folder to read
     * @param date date before which we are interested
     * @param script closure to run against each
     */
    fun forEachBefore(folderName: String, date: Instant, lambda: MailLambda) {

        val messages = store.getEmailsBeforeDate(folderName, date)
        runLambda(messages, lambda)
    }

    /**
     * Runs a script against all emails after a given date
     *
     * @param folderName folder to read
     * @param date date after which we are interested
     * @param script closure to run against each
     */
    fun forEachAfter(folderName: String, date: Instant, lambda: MailLambda) {
        val messages = store.getEmailsAfterDate(folderName, date)
        runLambda(messages, lambda)
    }

    //
    // Folder support
    //

    /**
     * Returns true if folder exists, false otherwise
     * @param folderName
     * @group Functions
     */
    fun hasFolder(folderName : String): Boolean = store.hasFolder(folderName)

    /**
     * Return all of the folder names for this account
     * @group Functions
     */
    fun getFolders(): List<FolderWrapper> = store.getFolders()

    /**
     * Read in all new messages. The first time this is run it will read in all messages. Subsequent calls will only return
     * messages that have been added after the first time messages were read.
     * @param folderName folder to read from
     * @param callback user supplied callback to handle the emails
     * @group Functions
     */
    //TODO -- add data component
//    fun readLatest(folderName: String, lambda: MailLambda) {
//        store.processLatest("LastRead", folderName, callback)
//    }

    /**
     * Runs a script against a list of emails in Inbox with the given uid
     *
     * @param ids array of specific uid's of the desired emails
     * @param script user callback
     * @group Functions
     */
    //TODO -- getEmailsByUID
//    fun foreach(ids: java.util.ArrayList[Number], script:ProcessCallback) {
//        fun get(folder: IMAPFolder) = store.getEmailsByUID(folder, ids)
//        store.foreach(Inbox, get, script.callback)
//    }

    //    /**
//     * Create an email object that can be sent
//     * @group Functions
//     */
//    fun newMail() = EmailBean(bean.user)
//
//    /**
//     * Send an email
//     * @param message email to send
//     * @group Functions
//     */
//    fun send(message: EmailBean) = MailUtils.sendMessage(bean, message)

    /**
     * Run a script against emails from a given list of ids and folder
     *
     * @param folderName folder to read from
     * @param ids email uids
     * @param script script to run against each
     * @group Functions
     */
    //    fun foreach(folderName: String, ids: java.util.ArrayList[Number], script: ProcessCallback) {
//        fun get(folder: IMAPFolder) = store.getEmailsByUID(folder, ids)
//        store.foreach(folderName, get, script.callback)
//    }

    //
    // Private
    //

}