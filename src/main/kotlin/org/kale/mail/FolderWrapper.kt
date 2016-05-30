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
 * Folder information
 */
class FolderWrapper(val store: StoreWrapper,

                    /**
                     * Name of this folder
                     * eg "Trash"
                     */
                    val name: String,

                    /**
                     * Fully qualified name, so it includes parents as well.
                     * Eg. [Gmail]/Trash
                     */
                    val fullName: String,

                    /**
                     * Is this the type of folder that can contain messages?
                     * For example, Google's [GMail] folder returns false
                     */
                    val canHoldMessages: Boolean,

                    /**
                     * Is this the type of folder than contains folders (and not messages)?
                     * For example, Google's [Gmail] folder returns true
                     * @return
                     */
                    val holdsFolders: Boolean,
                    val messageCount: Int,
                    val exists: Boolean) {


    //
    // Internal
    //


    companion object {
        fun create(folder: Folder, storeWrapper: StoreWrapper) =
            FolderWrapper(store= storeWrapper,
                    name = folder.name,
                    fullName = folder.fullName,
                    canHoldMessages =  (folder.getType() and Folder.HOLDS_MESSAGES) != 0,
                    holdsFolders = (folder.getType() and Folder.HOLDS_FOLDERS) != 0,
                    messageCount = folder.messageCount,
                    exists = folder.exists())
    }

}