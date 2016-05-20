package org.kale.mail

import com.sun.mail.imap.IMAPStore
import org.junit.Test
import org.kale.test.MockMailboxHelper
import java.time.Instant
import java.time.temporal.ChronoUnit
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

        val past = Instant.now().minus(1, ChronoUnit.DAYS)
        val future = Instant.now().plus(1, ChronoUnit.DAYS)

        val m1 = mock.createMessage(date = past)
        val m2 = mock.createMessage()
        val m3 = mock.createMessage(date = future)

        mock.addMessage(m1)
        mock.addMessage(m2)
        mock.addMessage(m3)


        val store = mock.session.getStore("mock_imaps");

        val storeHelper = StoreHelper(testAccount, store as IMAPStore)

        val emailsAll = storeHelper.getEmails("Inbox")

        assertEquals(3, emailsAll.count())

        val uid = emailsAll.get(1).uid

        val emailsByUID = storeHelper.getEmailsAfterUID("Inbox", uid);

        assertEquals(1, emailsByUID.count())

        // Test searching by date
        //TODO Update mocks to support setting received date

//        emails = storeHelper.getEmails("Inbox", 2)
//
//        assertEquals(2, emails.count())
//
//        emails = storeHelper.getEmailsAfterDate("Inbox", Instant.now())
//        assertEquals(1, emails.count())


    }
}