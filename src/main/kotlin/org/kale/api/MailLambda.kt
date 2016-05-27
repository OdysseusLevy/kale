package org.kale.api

/**
 */
@FunctionalInterface
interface  MailLambda {
    fun run(mail: Email): Unit
}