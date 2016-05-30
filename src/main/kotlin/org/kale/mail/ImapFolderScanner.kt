package org.kale.mail

import com.sun.mail.imap.IMAPFolder
import com.sun.mail.imap.protocol.IMAPProtocol
import org.apache.logging.log4j.LogManager
import java.time.Duration
import javax.mail.Folder
import javax.mail.FolderClosedException

/**
 * Utility to continuously scan for mails added to an imap folder.
 * It is meant to be run for months and even years without failing
 *
 * Uses the Imap IDLE command to wait until emails arrive
 * But sometimes a server will silently timeout while we are in IDLE and we stop getting messages
 *
 * So we use a separate thread to time out the IDLE by sending a NOOP command to the email server.
 *
 * This does two things. It is a keep-alive
 * for our server network connection and it also pops us out of our IDLE wait.
 *
 * We then can start a fresh IDLE session with the server
 *
 * See discussion here: http://stackoverflow.com/questions/4155412/javamail-keeping-imapfolder-idle-alive
 *
 * Some key learnings from earlier iterations:
 *
 * - The javamail Folder caches all mails that it reads, so we need to constantly close the folder to flush that
 * (otherwise it is a memory leak)
 *
 * - Imap servers will sometimes shutdown (for maintenance or whatever). We need to expect that
 *
 * - If a server goes down, all folders that are connected to that server need to be recreated
 */
class ImapFolderScanner(val store: StoreWrapper,
                        val folderName: String,
                        val callback: MailCallback,
                        var startUID: Long = -1,
                        val doFirstRead: Boolean = true
                        ): Runnable {

    var thread: Thread? = null

    companion object {
        val logger = LogManager.getLogger(StoreWrapper::class.java.name)
        val KEEP_ALIVE_FREQ = Duration.ofMinutes(9).toMillis() // rumor has it that gmail only allows 10 minute connections
        val WaitMillis = Duration.ofSeconds(5).toMillis()

    }
    // Thread to periodically sent NOOP messages to the server. The mail server will then send a message to us which will break
    // us out of our IDLE

    class TimeOut(val folderName: String, val store: StoreWrapper): Thread(folderName + "-Idle") {

        companion object {
            val logger = LogManager.getLogger(StoreWrapper::class.java.name)
            val noopCommand = object : IMAPFolder.ProtocolCommand {
                override fun doCommand(protocol: IMAPProtocol): Any? {
                    protocol.simpleCommand("NOOP", null)
                    return null
                }
            }
        }

        var timeOutThread: Thread? = null

        override fun run() {
        try {
            val folder = store.getFolder(folderName)
            if (folder !is IMAPFolder)
                throw Exception("Need imap folder for idle()")

            logger.debug("Thread ${getName()} is sleeping...")
            Thread.sleep(ImapFolderScanner.KEEP_ALIVE_FREQ)
            logger.debug("Thread ${getName()} waking up...")

            // Perform a NOOP to have us exit from idle
            logger.info("Performing a NOOP on ${folderName} to trigger exit from IDLE")
            if (folder.isOpen)
                folder.doCommand(noopCommand)
        }
        catch(e: InterruptedException) {
            // This is expected. InterruptedException means someone woke us from our sleep. Time for us to exit.
            logger.debug("Thread $folderName interrupted")
        }
        catch (e: Exception) {
            logger.warn("Unexpected exception in Thread $folderName", e)
        }

        logger.debug("Thread ${getName()} finished")
        }
    }

    fun startScanning() {
        thread = Thread(this, folderName + "-Scanner")
        thread?.start()
    }

    fun stopScanning() {
        thread?.interrupt()
    }

    fun processMessages(): Long {
        val messages = store.getMessagesAfterUID(folderName, startUID)
        callback.run(messages)

        if (messages.isEmpty())
            return startUID
        else
            return messages.get(messages.lastIndex).uid
    }

    override fun run() {

        // Run the callback right away
        if (doFirstRead) {
            startUID = processMessages()
        }

        // Now start our IDLE

        var needToWait = false
        var timeOut: TimeOut? = null
        var folder: Folder? = null

        while (!Thread.interrupted()) {
            logger.debug("Starting IDLE")
            try {

                if (needToWait) {
                    logger.info("Sleeping ${WaitMillis/1000} seconds to give email server time to restart")
                    Thread.sleep(WaitMillis)
                }
                needToWait = true

                folder = store.getFolder(folderName)
                if (folder !is IMAPFolder)
                    throw Exception("Need imap folder for idle()")

                timeOut = TimeOut(folderName, store)
                timeOut.start()


                folder.idle(true)
                needToWait = false

                logger.debug("returning from idle")
                timeOut.interrupt()
                timeOut = null

                startUID = processMessages()
            }
            catch (closed: FolderClosedException) {
                logger.warn(" Folder ${folderName} is closed.", closed)
            }
            catch ( error: javax.mail.StoreClosedException ) {
                logger.warn(" The javamail Store for Folder $folderName is closed", error)
            }
            catch (error: IllegalStateException) {
                logger.warn("Folder ${folderName} illegal state", error)
            }
            catch (e: Exception) {
                logger.error("Error running scanning callback on folder: $folderName", e)
                throw RuntimeException(e)
            } finally {
                store.closeFolder(folder)
                if (timeOut != null)
                    timeOut.interrupt()
            }
        }

        logger.info("stopped listening to ${folderName}")
    }

}