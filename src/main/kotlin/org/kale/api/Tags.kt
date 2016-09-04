package org.kale.api

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
interface Tags {

    //
    // Tags
    //

    /**
     * Add a tag to the set of tags for this object
     * @param tag name of tag
     * @group Functions
     */
    fun addTag(tag: String)

    /**
     * Checks to see if this object has the given tag set
     * @param tag name of tag
     * @return true if it exists
     * @group Functions
     */
    fun hasTag(tag: String): Boolean

    /**
     * Remove a given tag
     * @param tag name of tag
     * @group Functions
     */
    fun removeTag(tag: String)

    /**
     * get a value for a given key, "" returned if no tag or no value
     * @param tag name of tag
     * @group Functions
     */
    fun getValue(key: String): Any

    /**
     * Set a value for a given key
     */
    fun setValue(key: String, value: Any)

    /**
     * Get all tags set for this object
     * @group Functions
     */
    fun getTags(): Set<String>

}