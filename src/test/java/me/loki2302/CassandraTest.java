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

import static org.junit.Assert.assertEquals;

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
