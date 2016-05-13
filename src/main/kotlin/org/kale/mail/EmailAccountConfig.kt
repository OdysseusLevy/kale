package org.kale.mail

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
data class EmailAccountConfig(

        /**
         * Set user name. For example a gmail user name would be something like myname@gmail.com
         * @group Properties
         */
        val user: String = "",

        /**
         * Password (assumed to work with both imap and smtp)
         * @group Properties
         */
        val password: String = "",

        /**
         * Server used to receive mail (using the IMAP protocol). For example gmail uses: imap.gmail.com
         * @group Properties
         */
        val imapHost: String = "",

        /**
         * Optional, Advanced property -- usually the default will work
         * @group Properties
         */
        val imapPort: Int = -1,

        /**
         * Server used to send out email. For example gmail uses smtp.gmail.com
         * @group Properties
         */
        val smtpHost: String = "",
        val smtpPort: Int = 465);

