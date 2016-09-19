package org.mailcall.script

import javax.script.Bindings
import javax.script.SimpleBindings

/**
 * @author Odysseus Levy (odysseus.levy@am.sony.com)
 */
class SimpleConfiguration(val properties: Bindings = SimpleBindings()) : Configuration {

    fun put(key: String, value: Any) {
        properties.put(key, value)
    }

    fun putAll(values: Map<String, Any>) {
        properties.putAll(values);
    }

    override fun addObjectsToScope(bindings: Bindings) {
        bindings.putAll(properties);
    }
}