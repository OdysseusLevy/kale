package org.kale.api

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
data class Who(val email: String, val name: String, val tags: Tags) : Tags by tags {

}