package org.kale.api

/**
 * @author Odysseus Levy (odysseus.levy@am.sony.com)
 */
interface ProcessCallback {
    fun callback(email: Email): Unit
}