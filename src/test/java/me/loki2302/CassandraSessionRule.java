package me.loki2302;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.rules.ExternalResource;

public class CassandraSessionRule extends ExternalResource {
    private Cluster cluster;
    private Session session;

    @Override
    protected void before() throws Throwable {
        cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .build();

        session = cluster.connect();

        session.execute("drop keyspace if exists dummy");
        session.execute("create keyspace dummy " +
                "with replication = {'class':'SimpleStrategy', 'replication_factor':1}");
        session.execute("use dummy");
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
