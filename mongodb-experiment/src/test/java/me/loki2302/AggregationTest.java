package me.loki2302;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static me.loki2302.IterableUtils.listFromIterable;
import static org.junit.Assert.assertEquals;

public class AggregationTest extends AbstractMongoTest {
    @Test
    public void canAggregate() {
        MongoDatabase db = mongoClient.getDatabase(MONGO_DB);
        MongoCollection<Document> goods = db.getCollection("goods");
        goods.insertOne(makeGood("beer", 10));
        goods.insertOne(makeGood("vodka", 15));
        goods.insertOne(makeGood("coffee", 100));

        Document groupFields = new Document();
        groupFields.put("_id", 0);
        groupFields.put("average", new Document("$avg", "$price"));
        groupFields.put("min", new Document("$min", "$price"));
        groupFields.put("max", new Document("$max", "$price"));
        Document group = new Document("$group", groupFields);

        List<Document> results = listFromIterable(goods.aggregate(Collections.singletonList(group)));

        assertEquals(1, results.size());
        Document result = results.get(0);
        assertEquals(41.666, (double)result.get("average"), 1.0);
        assertEquals(10.0, (double)result.get("min"), 0.01);
        assertEquals(100.0, (double)result.get("max"), 0.01);
    }

    private static Document makeGood(String name, double price) {
        Document good = new Document();
        good.put("name", name);
        good.put("price", price);
        return good;
    }
}
