package org.kale.mail

interface MailCallback {
    fun run(email: List<MessageHelper>): Unit
}