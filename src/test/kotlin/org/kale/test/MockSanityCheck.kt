package org.kale.test

import org.junit.Test
import javax.mail.Folder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
class MockSanityCheck {

    @Test
    fun sanityCheck() {

        val mock = MockMailboxHelper()

        val m = mock.createMessage()
        mock.addMessage(m)
        mock.addMessage(m)
        mock.addMessage(m)

        val store = mock.session.getStore("mock_imaps");
        store.connect(mock.email, "test");
        val inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_WRITE);

        assertEquals(3, inbox.getMessageCount());
        assertNotNull(inbox.getMessage(1));
        inbox.close(true);
    }
}