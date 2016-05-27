package org.kale.mail

interface MailCallback {
    fun run(email: MessageHelper): Unit
}