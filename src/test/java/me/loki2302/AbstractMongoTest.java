package me.loki2302;

import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.net.UnknownHostException;

public abstract class AbstractMongoTest {
    private final static String MONGO_HOST = "localhost";
    private final static int MONGO_PORT = 12345;
    protected final static String MONGO_DB = "testdb";

    private static MongodProcess mongodProcess;
    protected MongoClient mongoClient;

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
}
