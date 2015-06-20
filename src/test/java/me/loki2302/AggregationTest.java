package me.loki2302;

import com.mongodb.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AggregationTest extends AbstractMongoTest {
    @Test
    public void canAggregate() {
        DB db = mongoClient.getDB(MONGO_DB);
        DBCollection goods = db.getCollection("goods");
        goods.insert(makeGood("beer", 10));
        goods.insert(makeGood("vodka", 15));
        goods.insert(makeGood("coffee", 100));

        DBObject groupFields = new BasicDBObject();
        groupFields.put("_id", 0);
        groupFields.put("average", new BasicDBObject("$avg", "$price"));
        groupFields.put("min", new BasicDBObject("$min", "$price"));
        groupFields.put("max", new BasicDBObject("$max", "$price"));
        DBObject group = new BasicDBObject("$group", groupFields);

        AggregationOutput output = goods.aggregate(group);
        Iterable<DBObject> it = output.results();
        List<DBObject> results = new ArrayList<DBObject>();
        for(DBObject result : it) {
            results.add(result);
        }

        assertEquals(1, results.size());
        DBObject result = results.get(0);
        assertEquals(41.666, (double)result.get("average"), 1.0);
        assertEquals(10.0, (double)result.get("min"), 0.01);
        assertEquals(100.0, (double)result.get("max"), 0.01);
    }

    private static DBObject makeGood(String name, double price) {
        BasicDBObject good = new BasicDBObject();
        good.put("name", name);
        good.put("price", price);
        return good;
    }
}
