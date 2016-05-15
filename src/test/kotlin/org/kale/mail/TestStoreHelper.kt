package org.kale.mail

import com.sun.mail.imap.IMAPStore
import org.junit.Test
import org.kale.test.MockMailboxHelper
import kotlin.test.assertEquals

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 *
 */
class TestStoreHelper {

    @Test
    public fun testGetMessages() {

        val testAccount = EmailAccountConfig(user = "test@kale.org")

        val mock = MockMailboxHelper(testAccount)

        val m = mock.createMessage()
        mock.addMessage(m)
        mock.addMessage(m)
        mock.addMessage(m)

        val store = mock.session.getStore("mock_imaps");

        val storeHelper = StoreHelper(testAccount, store as IMAPStore)

        var emails = storeHelper.getEmails("Inbox")

        assertEquals(3, emails.count())

        emails = storeHelper.getEmails("Inbox", 2)

        assertEquals(2, emails.count())
    }
}