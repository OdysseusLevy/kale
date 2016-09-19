package org.mailcall.script

import javax.script.Bindings

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
interface Configuration {

    fun addObjectsToScope(bindings: Bindings)
}