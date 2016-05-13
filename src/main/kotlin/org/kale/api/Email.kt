package org.kale.api

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
class Email(val subject: String,
            val toAll: Array<Who>,
            val ccAll: Array<Who>,
            val bccAll: Array<Who>,
            val replyToAll: Array<Who>,
            val fromAll: Array<Who>,
            val isRead: Boolean,
            val uid: Long,
            val folder: String,
            val headers: Map<String, String>,
            val size: Int
)

{

    //
    // Lazy Properties
    //

    /**
     * Body Text
     * @note -- this is a slower operation
     */
    //fun getBody(): String = helper.body


    /**
     * Attachments
     */
    //fun getAttachments(): Array[Attachment] = helper.attachments


    //
    // DKIM stuff
    //

    //TODO

    /**
     * Do a full DKIM verification of both the headers and the body
     * @return DkimResult
     */
    //fun getDkim: DkimInfo = DkimInfo(helper.dkimResult)

    /**
     * Do a DKIM verification on only the headers.
     *
     * This will be significantly faster than do a full verify, so sometimes it is preferred
     */
    //fun getDkimHeader = DkimInfo(helper.dkimHeader)

    /**
     * Same as getDkimHeader() except that we just want to know what the verified host is
     * @return if valid DKIM signature, the dkim host is returned else returns empty string
     */
    //fun getVerifiedHost(): String = helper.verifiedHost

    /**
     * Is the host verified by the DKIM standard?
     * Very useful for detecting spam
     */
    //fun getIsVerifiedHost(): Boolean = helper.verifiedHost != "" //For groovy java bean

    //
    // Time stuff
    //

    //TODO

    /**
     * When was this email received
     */
    //fun getReceived(): Date = helper.received

    /**
     * How many weeks ago was this message received?
     */
    //fun getWeeksAgo(): Long = helper.weeksAgo

    /**
     * Howe may days ago was this message received?
     */
    //fun getDaysAgo(): Long = helper.daysAgo

    /**
     * How many hours ago was this message received
     */
    //fun getHoursAgo(): Long = helper.hoursAgo

    //
    // Headers
    //

    //TODO

    /**
     * If this email was moved to a folder using Emailscript, a special header is added
     * You can use this property to what folder this email was moved from
     */
    //fun getMoveHeader(): String = helper.moveHeader.getOrElse("")

    //fun hasHeader(name: String): Boolean = helper.hasHeader(name)
    //fun getHeader(name: String): String = helper.getHeader(name).getOrElse(null)

    //
    // Commands
    //

    //TODO

    /**
     * Debugging utility
     */
    //fun dumpStructure(): Unit = helper.dumpStructure

    /**
     * Check if this email was sent to the given email
     * @param email
     */
    //fun wasSentTo(email: String): Boolean = helper.sentTo(email)

    /**
     * Move email to the given folder
     * @param folderName
     */
    //fun moveTo(folderName: String) = helper.moveTo(folderName)

    /**
     * Delete this email, if possible will move to "Trash" folder instead of permanently deleting it
     */
    //fun delete() = helper.delete()

    /**
     * Delete this email.
     * @param permanent if true, permanently deleted, otherwise the email is moved to the "Trash" folder
     */
    //fun delete(permanent: Boolean): Unit = helper.delete(permanent)

    //fun saveToFile(fileName: String): Unit = helper.saveToFile(fileName)

}