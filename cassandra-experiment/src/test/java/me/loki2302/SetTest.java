package me.loki2302;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SetTest {
    @Rule
    public CassandraSessionRule cassandraSessionRule = new CassandraSessionRule();

    @Test
    public void canUseASet() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text, tags set<text>)");
        session.execute("insert into notes(id, content, tags) values(1, 'hello', {'one', 'two'})");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        Set<String> note1Tags = rows.get(0).getSet("tags", String.class);
        assertEquals(2, note1Tags.size());
        assertTrue(note1Tags.contains("one"));
        assertTrue(note1Tags.contains("two"));
    }

    @Test
    public void canAddItemsToTheSet() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text, tags set<text>)");
        session.execute("insert into notes(id, content, tags) values(1, 'hello', {'one', 'two'})");
        session.execute("update notes set tags = tags + {'three', 'four'} where id = 1");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        Set<String> note1Tags = rows.get(0).getSet("tags", String.class);
        assertEquals(4, note1Tags.size());
        assertTrue(note1Tags.contains("one"));
        assertTrue(note1Tags.contains("two"));
        assertTrue(note1Tags.contains("three"));
        assertTrue(note1Tags.contains("four"));
    }

    @Test
    public void canRemoveItemsFromTheSet() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text, tags set<text>)");
        session.execute("insert into notes(id, content, tags) values(1, 'hello', {'one', 'two', 'three', 'four'})");
        session.execute("update notes set tags = tags - {'one', 'three'} where id = 1");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        Set<String> note1Tags = rows.get(0).getSet("tags", String.class);
        assertEquals(2, note1Tags.size());
        assertTrue(note1Tags.contains("two"));
        assertTrue(note1Tags.contains("four"));
    }

    @Test
    public void canReplaceTheSetItems() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text, tags set<text>)");
        session.execute("insert into notes(id, content, tags) values(1, 'hello', {'one', 'two'})");
        session.execute("update notes set tags = {'three', 'four'} where id = 1");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        Set<String> note1Tags = rows.get(0).getSet("tags", String.class);
        assertEquals(2, note1Tags.size());
        assertTrue(note1Tags.contains("three"));
        assertTrue(note1Tags.contains("four"));
    }

    @Test
    public void canSelectBySetContains() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text, tags set<text>)");
        session.execute("create index on notes(tags)");
        session.execute("insert into notes(id, content, tags) values(1, 'hello1', {'one', 'two'})");
        session.execute("insert into notes(id, content, tags) values(2, 'hello2', {'two', 'three'})");
        session.execute("insert into notes(id, content, tags) values(3, 'hello3', {'one', 'three'})");

        ResultSet resultSet = session.execute("select * from notes where tags contains 'two'");
        List<Row> rows = resultSet.all();
        assertEquals(2, rows.size());
    }

    @Test
    public void addingTheSameItemTwiceToTheSetDoesNotChangeAnything() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(id int primary key, content text, tags set<text>)");
        session.execute("insert into notes(id, content, tags) values(1, 'hello1', {'one', 'two'})");
        session.execute("update notes set tags = tags + {'one', 'two'} where id = 1");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        Set<String> note1Tags = rows.get(0).getSet("tags", String.class);
        assertEquals(2, note1Tags.size());
        assertTrue(note1Tags.contains("one"));
        assertTrue(note1Tags.contains("two"));
    }
}
