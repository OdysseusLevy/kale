package org.mailcall.dynamodb

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.PrimaryKey
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec
import org.apache.logging.log4j.LogManager
import org.kale.api.Who
import org.kale.mail.StoreWrapper
import java.util.*

/**
 * Created by odysseus on 6/10/16.
 */
class DynamoDbHelper {

    companion object {
        val logger = LogManager.getLogger(DynamoDbHelper::class.java.name)
    }

    val client = AmazonDynamoDBClient()
    val dynamoDB = DynamoDB(client);

    fun createWho(account: String, who: Who, tags: Set<String>, values: Map<String, String>) {

        val table = dynamoDB.getTable("Tags");

        val infoMap = HashMap<String, Any>();
        infoMap.put("tags",  tags)
        infoMap.put("values",  values)

        try {
            logger.debug("Adding a new who: $who");
            val outcome = table.putItem( Item()
                    .withPrimaryKey("account", account, "key", who.email)
                    .withMap("values", values)
                    .withStringSet("tags", tags))

            logger.debug("PutItem succeeded: ${outcome.getPutItemResult()}");

        } catch( e: Exception) {
            logger.error("Unable to add who to dynamodb: $who")
        }
    }

    fun getWho() {
        val client = AmazonDynamoDBClient()
        val dynamoDB = DynamoDB(client);

        val table = dynamoDB.getTable("Tags");
        val spec =  GetItemSpec()
                .withPrimaryKey("account", "odysseus@cosmosgame.org", "key", "vfitzharris@netscape.net");

        try {
            System.out.println("Attempting to read the item...");
            val outcome = table.getItem(spec);
            System.out.println("GetItem succeeded: " + outcome);

        } catch ( e: Exception) {
            System.err.println("Unable to read item: ");
            System.err.println(e.message);
        }
    }

}

fun main(args : Array<String>) {

    DynamoDbHelper().getWho()
//    val client = AmazonDynamoDBClient()
//    val mapper = DynamoDBMapper(client);
//
//    val item = mapper.load(DynamoTags::class.java, "odysseus@cosmosgame.org", "vfitzharris@netscape.net")
//
//    System.out.println(item)
}