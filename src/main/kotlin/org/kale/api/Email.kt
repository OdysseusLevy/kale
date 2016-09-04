package org.kale.api

import org.kale.dkim.DkimResult
import org.kale.mail.MessageHelper
import java.time.Instant

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
class Email(val helper: MessageHelper, val tags: Tags)
{
    //
    // Email basic fields
    //

    val subject: String = helper.subject
    val isRead: Boolean = helper.isRead
    val uid: Long = helper.uid
    val folder: String = helper.folderName
    val headers: Map<String, String> = helper.headers
    val size: Int = helper.size

    //
    // Addresses
    //

    val toAll: Array<Who> = Who.create(helper.toAll, tags)
    val ccAll: Array<Who> = Who.create(helper.ccAll, tags)
    val bccAll: Array<Who> = Who.create(helper.bccAll, tags)
    val replyToAll: Array<Who> = Who.create(helper.replyToAll, tags)
    val fromAll: Array<Who>  = Who.create(helper.fromAll, tags)

    /**
     * Typically there is only one address in the "to" field. If it is OK to assume this, then this field
     * is more convenient than toAll
     */
    val to: Who = Who.create(helper.to, tags)

    /**
     * Typically there is only one address in the "from" field. If it is OK to assume this, then this field
     * is more convenient than fromAll
     */
    val from: Who = Who.create(helper.from, tags)

    /**
     * Body Text
     * @note -- this is a slower operation
     */
    fun getBody(): String { return helper.body }

    //
    // DKIM stuff
    //

    /**
     * Do a DKIM verification on only the headers.
     *
     * This will be significantly faster than do a full verify, so sometimes it is preferred
     */
    val dkimHeader: DkimResult = helper.dkimHeader

    /**
     * Do a full DKIM verification of both the headers and the body
     * @return DkimResult
     */
    fun getDkim(): DkimResult {return helper.dkimResult}

    /**
     * Is the host verified by the DKIM standard?
     * Very useful for detecting spam
     */
    val isVerifiedHost: Boolean = helper.verifiedHost != "" //For groovy java bean

    /**
     * Same as getDkimHeader() except that we just want to know what the verified host is
     * @return if valid DKIM signature, the dkim host is returned else returns empty string
     */
    val verifiedHost: String = helper.verifiedHost

    //
    // Time stuff
    //

    /**
     * When was this email received
     */
    val received: Instant = helper.received

    /**
     * How many weeks ago was this message received?
     */
    val weeksAgo: Long = helper.weeksAgo

    /**
     * How may days ago was this message received?
     */
    val daysAgo: Long = helper.daysAgo

    /**
     * How many hours ago was this message received
     */
    val hoursAgo: Long = helper.hoursAgo

    //
    // Headers
    //

    /**
     * If this email was moved to a folder using Emailscript, a special header is added
     * You can use this property to determine folder this email was moved from
     */
    val moveHeader: String = helper.moveHeader


    /**
     * Does this email container a header with this name?
     */
    fun hasHeader(name: String): Boolean = helper.hasHeader(name)


    fun getHeader(name: String): String = helper.getHeader(name)

    //
    // Commands
    //

    /**
     * Check if this email was sent to the given email
     * @param email
     */
    fun wasSentTo(email: String): Boolean = helper.wasSentTo(email)

    /**
     * Move email to the given folder
     * @param folderName
     */
    fun moveTo(folderName: String) = helper.moveTo(folderName)

    /**
     * Delete this email, if possible will move to "Trash" folder instead of permanently deleting it
     */
    fun delete() = helper.delete()

    /**
     * Delete this email.
     * @param permanent if true, permanently deleted, otherwise the email is moved to the "Trash" folder
     */
    fun delete(permanent: Boolean): Unit = helper.delete(permanent)

    //
    //TODO
    //

    //fun saveToFile(fileName: String): Unit = helper.saveToFile(fileName)

    //fun dumpStructure(): Unit = helper.dumpStructure

    //fun getAttachments(): Array[Attachment] = helper.attachments
}