package org.kale.api

import org.kale.mail.AddressHelper

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
data class Who(val email: String, val name: String, val tags: Tags) : Tags by tags {

    companion object {
        fun create(address: AddressHelper, tags: Tags): Who = Who(address.email, address.name, tags);
        fun create(list: List<AddressHelper>, tags: Tags ): Array<Who> = list.map { create(it, tags)}.toTypedArray()
    }
}