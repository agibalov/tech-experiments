package me.loki2302;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static me.loki2302.IterableUtils.listFromIterable;
import static me.loki2302.IterableUtils.setFromIterable;
import static me.loki2302.IterableUtils.streamFromIterable;
import static org.junit.Assert.*;

public class HelloWorldTest extends AbstractMongoTest {
    @Test
    public void canCreateDatabase() {
        Set<String> databaseNames = setFromIterable(mongoClient.listDatabaseNames());
        assertFalse(databaseNames.contains(MONGO_DB));
        
        MongoDatabase db = mongoClient.getDatabase(MONGO_DB);
        db.getCollection("x").insertOne(new Document("x", 1));
        
        databaseNames = setFromIterable(mongoClient.listDatabaseNames());
        assertTrue(databaseNames.contains(MONGO_DB));
    }
    
    @Test
    public void canCreateCollection() {
        MongoDatabase db = mongoClient.getDatabase(MONGO_DB);
        Set<String> collectionNames = setFromIterable(db.listCollectionNames());
        assertFalse(collectionNames.contains("x"));
        
        db.getCollection("x").insertOne(new Document("x", 1));
                
        collectionNames = setFromIterable(db.listCollectionNames());
        assertTrue(collectionNames.contains("x"));
    }
    
    @Test
    public void canInsertDocument() {
        MongoDatabase db = mongoClient.getDatabase(MONGO_DB);
        MongoCollection<Document> people = db.getCollection("people");
        Document person = new Document("name", "loki2302").append("age", 33);
        people.insertOne(person);
        assertEquals(1, people.count());        
    }
    
    @Test
    public void canRetrieveDocument() {
        MongoDatabase db = mongoClient.getDatabase(MONGO_DB);
        MongoCollection<Document> people = db.getCollection("people");
        Document person = new Document("name", "loki2302").append("age", 33);
        people.insertOne(person);
        
        Document retrievedPerson = listFromIterable(people.find()).get(0);
        assertEquals("loki2302", retrievedPerson.get("name"));
        assertEquals(33, retrievedPerson.get("age"));
    }
    
    @Test
    public void canUpdateDocument() {
        MongoDatabase db = mongoClient.getDatabase(MONGO_DB);
        MongoCollection<Document> people = db.getCollection("people");
        Document person = new Document("name", "loki2302").append("age", 33);
        people.insertOne(person);

        people.findOneAndUpdate(new Document("name", "loki2302"),
                new Document("$set", new Document("name", "2302loki").append("age", 22)));
        assertEquals(1, people.count());

        Document updatedPerson = listFromIterable(people.find()).get(0);
        assertEquals("2302loki", updatedPerson.get("name"));
        assertEquals(22, updatedPerson.get("age"));
    }
    
    @Test
    public void canFindDocumentsByAttribute() {
        MongoDatabase db = mongoClient.getDatabase(MONGO_DB);
        MongoCollection<Document> people = db.getCollection("people");
        people.insertOne(new Document("name", "loki2302_1").append("age", 33));
        people.insertOne(new Document("name", "loki2302_2").append("age", 22));
        people.insertOne(new Document("name", "loki2302_3").append("age", 33));

        List<String> names = streamFromIterable(people.find(new Document("age", 33)))
                .map(p -> (String)p.get("name"))
                .collect(Collectors.toList());
        
        assertEquals(2, names.size());
        assertTrue(names.contains("loki2302_1"));
        assertTrue(names.contains("loki2302_3"));
    }

    @Test
    public void canFindDocumentsByManyAttributes() {
        MongoDatabase db = mongoClient.getDatabase(MONGO_DB);
        MongoCollection<Document> people = db.getCollection("people");
        people.insertOne(new Document("name", "loki2302_1").append("age", 33).append("sex", "m"));
        people.insertOne(new Document("name", "loki2302_2").append("age", 33).append("sex", "m"));
        people.insertOne(new Document("name", "loki2302_3").append("age", 33).append("sex", "f"));

        List<String> names = streamFromIterable(people.find(new Document("age", 33).append("sex", "m")))
                .map(p -> (String)p.get("name"))
                .collect(Collectors.toList());

        assertEquals(2, names.size());
        assertTrue(names.contains("loki2302_1"));
        assertTrue(names.contains("loki2302_2"));
    }

    @Test
    public void canFindDocumentsByLikeAttribute() {
        MongoDatabase db = mongoClient.getDatabase(MONGO_DB);
        MongoCollection<Document> people = db.getCollection("people");
        people.insertOne(new Document("name", "loki2302"));
        people.insertOne(new Document("name", "john"));
        people.insertOne(new Document("name", "mocking"));

        List<String> names = streamFromIterable(people.find(new Document("name", Pattern.compile("ki"))))
                .map(p -> (String)p.get("name"))
                .collect(Collectors.toList());

        assertEquals(2, names.size());
        assertTrue(names.contains("loki2302"));
        assertTrue(names.contains("mocking"));
    }

    @Test
    public void canDeleteDocuments() {
        MongoDatabase db = mongoClient.getDatabase(MONGO_DB);
        MongoCollection<Document> people = db.getCollection("people");
        Document person = new Document("name", "loki2302").append("age", 33);
        people.insertOne(person);
        people.deleteOne(new Document("name", "loki2302"));
        assertEquals(0, people.count());
    }

    // TODO: use index
}
