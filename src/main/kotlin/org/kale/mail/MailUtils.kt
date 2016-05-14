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


}