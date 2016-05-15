package org.kale.test

import de.saly.javamail.mock2.MailboxFolder
import de.saly.javamail.mock2.MockMailbox
import org.kale.mail.EmailAccountConfig
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
class MockMailboxHelper(val email: String = "test@kale.org") {

    constructor(account: EmailAccountConfig): this(account.user) {

    }

    val session = Session.getInstance(Properties());
    val mb = MockMailbox.get(email);

    fun createMessage(subject: String = "test subject",
                      text: String = " test body",
                      from: String = "test-from@kale.org"): MimeMessage {

        val msg = MimeMessage(session);
        msg.setSubject(subject);
        msg.setFrom(from);
        msg.setText(text);
        msg.setRecipient(Message.RecipientType.TO, InternetAddress(email));
        return msg
    }

    fun addMessage(message: MimeMessage, folder: MailboxFolder = mb.inbox) {
        folder.add(message);
    }
}