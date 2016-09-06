package org.mailcall.dynamodb

import org.kale.api.Tags
import java.util.*


/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
class DynamoTags(val tagSet: HashSet<String>, val values: HashMap<String, Any>) : Tags {

    fun doUpdate() {
//TODO
    }

    override fun hasTag(tag: String): Boolean {
        return tagSet.contains(tag)
    }

    override fun removeTag(tag: String) {

        if (tagSet.remove(tag))
            doUpdate()
    }

    override fun getValue(key: String): Any {
        return values.get(key) ?: ""
    }

    override fun setValue(key: String, value: Any) {
        values.put(key, value)
        doUpdate()
    }

    override fun getTags(): Set<String> {
       return tagSet
    }

    override fun addTag(tag: String) {
        tagSet.add(tag)
        doUpdate()
    }


}