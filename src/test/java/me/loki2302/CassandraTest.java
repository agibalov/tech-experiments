package me.loki2302;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

// TODO: try http://docs.datastax.com/en/developer/java-driver/2.1/java-driver/reference/crudOperations.html

public class CassandraTest {
    private Cluster cluster;
    private Session session;

    @Before
    public void connect() {
        cluster = Cluster.builder()
                .addContactPoint("127.0.0.1")
                .build();

        session = cluster.connect();

        session.execute("drop keyspace if exists dummy");
        session.execute("create keyspace dummy " +
                "with replication = {'class':'SimpleStrategy', 'replication_factor':1}");
        session.execute("use dummy");
    }

    @After
    public void disconnect() {
        session.close();
        session = null;

        cluster.close();
        cluster = null;
    }

    @Test
    public void canSaveAndLoadAFewRows() {
        session.execute("create table notes(note_id int primary key, content text)");
        session.execute("insert into notes(note_id, content) values(111, 'hello there')");
        session.execute("insert into notes(note_id, content) values(222, 'second note')");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();

        assertEquals(2, rows.size());

        assertEquals(111, rows.get(0).getInt("note_id"));
        assertEquals("hello there", rows.get(0).getString("content"));

        assertEquals(222, rows.get(1).getInt("note_id"));
        assertEquals("second note", rows.get(1).getString("content"));
    }
}
