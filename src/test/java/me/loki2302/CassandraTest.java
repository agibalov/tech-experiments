package me.loki2302;

import com.datastax.driver.core.*;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.google.common.base.Objects;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CassandraTest {
    @Rule
    public CassandraSessionRule cassandraSessionRule = new CassandraSessionRule();

    @Test
    public void canSaveAndLoadAFewRows() {
        Session session = cassandraSessionRule.getSession();
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

    // TODO: can I select by 'list contains'?
    // TODO: append an item
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
    public void canUseAMap() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table users(id int primary key, properties map<text, text>)");
        session.execute("insert into users(id, properties) values(1, {'name':'loki2302', 'url':'http://loki2302.me'})");

        ResultSet resultSet = session.execute("select * from users");
        List<Row> rows = resultSet.all();
        Map<String, String> propertyMap = rows.get(0).getMap("properties", String.class, String.class);
        assertEquals(2, propertyMap.size());
        assertEquals("loki2302", propertyMap.get("name"));
        assertEquals("http://loki2302.me", propertyMap.get("url"));
    }

    @Test
    public void canUseUDT() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create type user(id int, name text)");
        session.execute("create table notes(id int primary key, content text, author frozen<user>)");
        session.execute("insert into notes(id, content, author) values(1, 'hello', {id: 2302, name: 'loki2302'})");

        ResultSet resultSet = session.execute("select * from notes");
        List<Row> rows = resultSet.all();
        Row firstRow = rows.get(0);
        UDTValue author = firstRow.getUDTValue("author");
        assertEquals("user", author.getType().getTypeName());
        assertEquals(2302, author.getInt("id"));
        assertEquals("loki2302", author.getString("name"));
    }

    @Test
    public void canSaveAndLoadAFewRowsUsingMapper() {
        Session session = cassandraSessionRule.getSession();
        session.execute("create table notes(note_id int primary key, content text)");

        MappingManager mappingManager = new MappingManager(session);
        Mapper<Note> noteMapper = mappingManager.mapper(Note.class);

        if(true) {
            Note note = new Note();
            note.setId(11);
            note.setContent("hello there");
            noteMapper.save(note);
        }

        Note note = noteMapper.get(11);
        assertEquals(11, (int) note.getId());
        assertEquals("hello there", note.getContent());
    }

    @Table(keyspace = "dummy", name = "notes")
    public static class Note {
        @PartitionKey
        @Column(name = "note_id")
        private Integer id;

        @Column(name = "content")
        private String content;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public boolean equals(Object o) {
            if(!(o instanceof Note)) {
                return false;
            }

            Note other = (Note)o;
            return Objects.equal(id, other.id) &&
                    Objects.equal(content, other.content);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id, content);
        }
    }
}
