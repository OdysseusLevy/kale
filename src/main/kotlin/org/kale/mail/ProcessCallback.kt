package org.kale.mail

/**
 */
interface ProcessCallback {
    fun processLatest(latest: List<MessageHelper>)
}