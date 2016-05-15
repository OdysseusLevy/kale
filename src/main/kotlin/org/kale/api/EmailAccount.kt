package org.kale.api

import org.kale.mail.StoreHelper

/**
 * @author Odysseus Levy (odysseus.levy@am.sony.com)
 */
class EmailAccount(store: StoreHelper) {

//    /**
//     * Runs a script against all inbox emails
//     * @group Functions
//     */
//    fun forEach(script: ProcessCallback): Unit {
//        def get(folder: IMAPFolder) = store.getEmails(folder)
//        store.foreach(Inbox ,get, script.callback)
//    }
//
//
//
//    fun foreach(folderName: String,
//                source: (Folder) -> Array<MessageHelper>,
//                callback: (MessageHelper)->Unit): Long {
//
//        var uid: Long = 0
//        val folder = getFolder(folderName)
//
//        try {
//            source(folder).forEach { email: MessageHelper ->
//                uid = email.uid
//                callback(email)
//            }
//        } catch (e: Exception) {
//            StoreHelper.logger.warn("error running script on folder: $folderName for: ${account.user}", e)
//        } finally {
//            folder.close(!dryRun)
//        }
//
//        return uid
//    }
}