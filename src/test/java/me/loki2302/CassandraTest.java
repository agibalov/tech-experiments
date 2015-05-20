package me.loki2302;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.google.common.base.Objects;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    // TODO: can I select by 'set contains'?
    // TODO: add an item to set
    // TODO: remove an item from set
    // TODO: add a duplicate should result in no changes
    // TODO: can I only fetch a subset of set items?
    @Test
    public void canUseASet() {
        session.execute("create table notes(id int primary key, content text, tags set<text>)");
        session.execute("insert into notes(id, content, tags) values(1, 'hello', {'one', 'two'})");

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
    public void canSaveAndLoadAFewRowsUsingMapper() {
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
