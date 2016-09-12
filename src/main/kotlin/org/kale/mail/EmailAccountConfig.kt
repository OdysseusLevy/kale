package org.kale.mail

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
data class EmailAccountConfig(

        var nickname: String ="",

        /**
         * Set user name. For example a gmail user name would be something like myname@gmail.com
         * @group Properties
         */
        var user: String = "",

        /**
         * Password (assumed to work with both imap and smtp)
         * @group Properties
         */
        var password: String = "",

        /**
         * Server used to receive mail (using the IMAP protocol). For example gmail uses: imap.gmail.com
         * @group Properties
         */
        var imapHost: String = "",

        /**
         * Optional, Advanced property -- usually the default will work
         * @group Properties
         */
        var imapPort: Int = -1,

        /**
         * Server used to send out email. For example gmail uses smtp.gmail.com
         * @group Properties
         */
        var smtpHost: String = "",
        var smtpPort: Int = 465);

