package me.loki2302;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

public class AppTest {
    private final static String MONGO_HOST = "localhost";
    private final static int MONGO_PORT = 12345;
    private final static String MONGO_DB = "testdb";
    
    private static MongodProcess mongodProcess;
    private MongoClient mongoClient;    
    
    @BeforeClass
    public static void startMongo() throws UnknownHostException, IOException {
        IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(MONGO_PORT, Network.localhostIsIPv6()))
            .build();
    
        MongodStarter mongodStarter = MongodStarter.getDefaultInstance();
        MongodExecutable mongodExecutable = mongodStarter.prepare(mongodConfig);
        mongodProcess = mongodExecutable.start();
    }
    
    @AfterClass
    public static void stopMongo() {        
        mongodProcess.stop();
    }
    
    @Before
    public void setUp() throws UnknownHostException {
        mongoClient = new MongoClient(MONGO_HOST, MONGO_PORT);
        mongoClient.dropDatabase(MONGO_DB);
    }
    
    @After
    public void cleanUp() {
        mongoClient.dropDatabase(MONGO_DB);
    }
    
    @Test
    public void canCreateDatabase() {
        List<String> databaseNames = mongoClient.getDatabaseNames();
        assertFalse(databaseNames.contains(MONGO_DB));
        
        DB db = mongoClient.getDB(MONGO_DB);
        db.getCollection("x").insert(new BasicDBObject("x", 1));
        
        databaseNames = mongoClient.getDatabaseNames();
        assertTrue(databaseNames.contains(MONGO_DB));
    }
    
    @Test
    public void canCreateCollection() {
        DB db = mongoClient.getDB(MONGO_DB);
        Set<String> collectionNames = db.getCollectionNames();
        assertFalse(collectionNames.contains("x"));
        
        db.getCollection("x").insert(new BasicDBObject("x", 1));
                
        collectionNames = db.getCollectionNames();
        assertTrue(collectionNames.contains("x"));        
    }
    
    @Test
    public void canInsertDocument() {
        DB db = mongoClient.getDB(MONGO_DB);
        DBCollection people = db.getCollection("people");
        BasicDBObject person = new BasicDBObject("name", "loki2302").append("age", 33);
        people.insert(person);
        assertEquals(1, people.count());        
    }
    
    @Test
    public void canRetrieveDocument() {
        DB db = mongoClient.getDB(MONGO_DB);
        DBCollection people = db.getCollection("people");
        BasicDBObject person = new BasicDBObject("name", "loki2302").append("age", 33);
        people.insert(person);
        
        DBObject retrievedPerson = people.findOne();
        assertEquals("loki2302", retrievedPerson.get("name"));
        assertEquals(33, retrievedPerson.get("age"));
    }
    
    @Test
    public void canUpdateDocument() {
        DB db = mongoClient.getDB(MONGO_DB);
        DBCollection people = db.getCollection("people");
        BasicDBObject person = new BasicDBObject("name", "loki2302").append("age", 33);
        people.insert(person);
        
        DBObject retrievedPerson = people.findOne();
        retrievedPerson.put("name", "2302loki");
        retrievedPerson.put("age", 22);
        people.save(retrievedPerson);
        assertEquals(1, people.count());
                
        DBObject updatedPerson = people.findOne();
        assertEquals("2302loki", updatedPerson.get("name"));
        assertEquals(22, updatedPerson.get("age"));
    }
    
    @Test
    public void canFindDocumentsByAttribute() {        
        DB db = mongoClient.getDB(MONGO_DB);
        DBCollection people = db.getCollection("people");
        people.insert(new BasicDBObject("name", "loki2302_1").append("age", 33));
        people.insert(new BasicDBObject("name", "loki2302_2").append("age", 22));
        people.insert(new BasicDBObject("name", "loki2302_3").append("age", 33));
        
        List<String> names = new ArrayList<String>();
        DBCursor cursor = people.find(new BasicDBObject("age", 33));
        try {
            while(cursor.hasNext()) {
                DBObject person = cursor.next();
                names.add((String)person.get("name"));
            }
        } finally {
            cursor.close();
        }
        
        assertEquals(2, names.size());
        assertTrue(names.contains("loki2302_1"));
        assertTrue(names.contains("loki2302_3"));
    }

    @Test
    public void canFindDocumentsByLikeAttribute() {
        DB db = mongoClient.getDB(MONGO_DB);
        DBCollection people = db.getCollection("people");
        people.insert(new BasicDBObject("name", "loki2302"));
        people.insert(new BasicDBObject("name", "john"));
        people.insert(new BasicDBObject("name", "mocking"));

        List<String> names = new ArrayList<String>();
        BasicDBObject criterion = new BasicDBObject();
        criterion.put("name", Pattern.compile("ki"));
        DBCursor cursor = people.find(criterion);
        try {
            while(cursor.hasNext()) {
                DBObject person = cursor.next();
                names.add((String)person.get("name"));
            }
        } finally {
            cursor.close();
        }

        assertEquals(2, names.size());
        assertTrue(names.contains("loki2302"));
        assertTrue(names.contains("mocking"));
    }
    
    @Test
    public void canDeleteDocuments() {
        DB db = mongoClient.getDB(MONGO_DB);
        DBCollection people = db.getCollection("people");
        BasicDBObject person = new BasicDBObject("name", "loki2302").append("age", 33);
        people.insert(person);        
        people.remove(new BasicDBObject("name", "loki2302"));        
        assertEquals(0, people.count());
    }
    
    // todo: aggregation [sum, avg]
    // todo: find by multiple fields [just-multiple, nested]
    // todo: use index
}
