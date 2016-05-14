package org.kale.mail

import javax.mail.Address
import javax.mail.internet.InternetAddress

/**
 * @author Odysseus Levy (odysseus.levy@am.sony.com)
 */
class AddressHelper(val address: Address){

    val ia: InternetAddress = address as InternetAddress
    val email: String = MailUtils.getOrElse(ia.address, "")
    val name: String = MailUtils.getOrElse(ia.personal, "")


    companion object {

        val NoOne = AddressHelper("None", "None")

        public fun getFirst (addressArray: Array<Address>) = {
            if (addressArray == null || addressArray.count() == 0) {
                NoOne
            }
            else {
                AddressHelper(addressArray[0])
            }
        }

        public fun getAll (addressArray: Array<Address>): List<AddressHelper> {
            return if (addressArray == null || addressArray.count() == 0) {
                listOf(NoOne)
            }
            else {
                addressArray.map {AddressHelper(it)}
            }
        }
    }
}