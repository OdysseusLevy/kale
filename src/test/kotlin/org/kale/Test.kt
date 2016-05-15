package org.kale

import de.saly.javamail.mock2.MockMailbox
import org.junit.Test
import java.util.*
import javax.mail.Folder
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * @author Odysseus Levy (odysseus.levy@am.sony.com)
 */
class Test {

    @Test
    public fun setupMailboxes() {

        val session = Session.getInstance(Properties());

        val mb = MockMailbox.get("test@kale.org");
        val mf = mb.getInbox();

        val msg = MimeMessage(session);
        msg.setSubject("Test");
        msg.setFrom("from@sender.com");
        msg.setText("Some text here ...");
        msg.setRecipient(Message.RecipientType.TO, InternetAddress("hendrik@unknown.com"));
        mf.add(msg); // 11
        mf.add(msg); // 12
        mf.add(msg); // 13


        val store = session.getStore("mock_imaps");
        store.connect("test@kale.org", "test");
        val inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_WRITE);

        assertEquals(3, inbox.getMessageCount());
        assertNotNull(inbox.getMessage(1));
        inbox.close(true);
    }


}