package org.kale.mail

import com.sun.mail.imap.IMAPFolder
import javax.mail.Message

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
object MailUtils {


    public fun getOrElse(value: Object, other: Object) = if (value != null) value else other
    public fun getOrElse(value: String, other: String) = if (value != null) value else other

    public fun getUID(m: Message): Long {

        val folder = m.folder
        return when(folder) {
            is IMAPFolder -> folder.getUID(m)
            else -> -1
        }
    }

    //
    // Folder names
    //

    private val Trash = "Trash"
    private val GmailTrash = "[Gmail]/Trash"

    private val Spam = "Spam"
    private val GmailSpam = "[Gmail]/Spam"

    fun trashFolder(account: EmailAccountConfig): String {
        return if (isGmail(account))
            GmailTrash
        else
            Trash
    }

    val gmailTopLevelFolders = setOf("inbox", "deleted messages", "drafts", "sent", "sent messages")

    fun isGmail(account: EmailAccountConfig) = account.user.toLowerCase().endsWith("gmail.com")

    fun getFolderName(account: EmailAccountConfig, name: String): String  {
        if (!isGmail(account))
            return name

        // Handle Gmail specific folders

        val lname = name.toLowerCase()
        return when(lname) {
            "trash" -> "[Gmail]/Trash"
            "spam" -> "[Gmail]/Spam"
            "drafts" -> "[Gmail]/Drafts"
            else -> lname
        }
    }
}