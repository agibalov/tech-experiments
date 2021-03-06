package me.loki2302;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

// TODO: where
// TODO: order by
public class DummyTest {
    @Rule
    public CassandraSessionRule cassandraSessionRule = new CassandraSessionRule();

    @Test
    public void canSaveAndLoadAFewRows() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text)");
        session.execute("insert into notes(id, content) values(111, 'hello there')");
        session.execute("insert into notes(id, content) values(222, 'second note')");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();

        assertEquals(2, rows.size());

        assertEquals(111, rows.get(0).getInt("id"));
        assertEquals("hello there", rows.get(0).getString("content"));

        assertEquals(222, rows.get(1).getInt("id"));
        assertEquals("second note", rows.get(1).getString("content"));
    }

    @Test
    public void canDeleteARow() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text)");
        session.execute("insert into notes(id, content) values(111, 'hello there')");
        session.execute("insert into notes(id, content) values(222, 'second note')");
        session.execute("delete from notes where id = 111");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        assertEquals(1, rows.size());
        assertEquals(222, rows.get(0).getInt("id"));
    }
}
