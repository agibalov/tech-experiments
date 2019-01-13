package me.loki2302;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.google.common.base.Objects;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RowMapperTest {
    @Rule
    public CassandraSessionRule cassandraSessionRule = new CassandraSessionRule();

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

    @Table(name = "notes")
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
