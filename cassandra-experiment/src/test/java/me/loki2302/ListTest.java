package me.loki2302;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ListTest {
    @Rule
    public CassandraSessionRule cassandraSessionRule = new CassandraSessionRule();

    // TODO: remove an item
    // TODO: get item by index
    // TODO: can I only fetch a part of the list
    @Test
    public void canUseAList() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text, tags list<text>)");
        session.execute("insert into notes(id, content, tags) values(1, 'hello', ['one', 'two'])");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        List<String> note1Tags = rows.get(0).getList("tags", String.class);
        assertEquals(2, note1Tags.size());
        assertEquals("one", note1Tags.get(0));
        assertEquals("two", note1Tags.get(1));
    }

    @Test
    public void canSelectByListContains() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text, tags list<text>)");
        session.execute("create index on notes(tags)");
        session.execute("insert into notes(id, content, tags) values(1, 'hello1', ['one', 'two'])");
        session.execute("insert into notes(id, content, tags) values(2, 'hello2', ['two', 'three'])");
        session.execute("insert into notes(id, content, tags) values(3, 'hello3', ['one', 'three'])");

        ResultSet resultSet = session.execute("select * from notes where tags contains 'two'");
        List<Row> rows = resultSet.all();
        assertEquals(2, rows.size());
    }

    @Test
    public void canAddAnItemToTheList() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, tags list<text>)");
        session.execute("insert into notes(id, tags) values(1, ['one', 'two'])");
        session.execute("update notes set tags = tags + ['three'] where id = 1");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        List<String> note1Tags = rows.get(0).getList("tags", String.class);
        assertEquals(3, note1Tags.size());
        assertEquals("one", note1Tags.get(0));
        assertEquals("two", note1Tags.get(1));
        assertEquals("three", note1Tags.get(2));
    }
}
