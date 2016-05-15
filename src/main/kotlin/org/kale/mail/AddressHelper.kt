package org.kale.mail

import javax.mail.Address
import javax.mail.internet.InternetAddress

/**
 * @author Odysseus Levy (odysseus.levy@am.sony.com)
 */
class AddressHelper(val email: String, val name: String) {

    companion object {

        fun create(a: Address): AddressHelper {
            val ia: InternetAddress = a as InternetAddress

            val email = if (ia.address != null) ia.address else ""
            val name: String = if (ia.personal != null) ia.personal else ""

            return AddressHelper(email, name)
        }

        val NoOne = AddressHelper("None", "None")

        fun getFirst (addressArray: Array<Address>) = {
            if (addressArray == null || addressArray.count() == 0) {
                NoOne
            }
            else {
                create(addressArray[0] as InternetAddress)
            }
        }

        fun getAll (addressArray: Array<Address>): List<AddressHelper> {
            return if (addressArray == null || addressArray.count() == 0) {
                listOf(NoOne)
            }
            else {
                addressArray.map {create(it)}
            }
        }
    }
}