package org.mailcall.script

import javax.script.Bindings

/**
 * @author Odysseus Levy (odysseus.levy@am.sony.com)
 */
class SimpleConfiguration(val properties: Map<String, Any>) : Configuration {
    override fun addObjects(bindings: Bindings) {
        bindings.putAll(properties)
    }
}