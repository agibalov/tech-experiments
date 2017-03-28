package me.loki2302;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.rules.ExternalResource;

import java.util.UUID;

public class CassandraSessionRule extends ExternalResource {
    private Cluster cluster;
    private Session session;

    @Override
    protected void before() throws Throwable {
        cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .build();

        session = cluster.connect();

        // It appears that drop-create keyspace scenario is very unstable
        // in all (many?) versions of Cassandra, so the default suggestion is to
        // truncate each table. This solution is not good enough for my tests,
        // so I just create a new keyspace every time I need it.

        String keyspaceRandom = UUID.randomUUID().toString().replace("-", "");
        String keyspaceName = String.format("keyspace%s", keyspaceRandom);
        session.execute(String.format(
                "create keyspace if not exists %s " +
                "with replication = {'class':'SimpleStrategy', 'replication_factor':1}",
                keyspaceName));
        session.execute(String.format("use %s", keyspaceName));
    }

    @Override
    protected void after() {
        session.close();
        session = null;

        cluster.close();
        cluster = null;
    }

    public Session getSession() {
        return session;
    }
}
