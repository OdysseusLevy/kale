package org.mailcall.dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec
import org.apache.logging.log4j.LogManager
import java.util.*

/**
 * @author Odysseus Levy (odysseus@cosmosgame.org)
 */
class DynamoHelper(val account: String, val cache: Map<String, Item>? = HashMap<String, Item>()) {
    val logger = LogManager.getLogger(DynamoHelper::class.java.name)
    val client = AmazonDynamoDBClient()
    val dynamoDB = DynamoDB(client);

    val table = dynamoDB.getTable("Tags");

    fun getItem(key: String): Item? {

        if (cache != null && cache.containsKey(key)) {
            return cache.get(key)
        }

        val spec = GetItemSpec()
                .withPrimaryKey("account", account, "key", key);

        try {
            return table.getItem(spec);

        } catch (e: Exception) {
            logger.error("error trying to read: $key for account $account", e)
        }

        return null;
    }


}