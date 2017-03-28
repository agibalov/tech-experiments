package me.loki2302.spring;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cassandra.config.java.AbstractClusterConfiguration;
import org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification;
import org.springframework.cassandra.core.keyspace.DropKeyspaceSpecification;
import org.springframework.cassandra.core.keyspace.KeyspaceOption;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class DummySpringTest {
    @Autowired
    private CassandraTemplate cassandraTemplate;

    @Autowired
    private NoteRepository noteRepository;

    @Test
    public void dummy() {
        if(true) {
            Note note = new Note();
            note.id = UUIDs.timeBased();
            note.content = "hello";
            noteRepository.save(note);
        }

        List<Note> notes = StreamSupport.stream(noteRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
        assertEquals(1, notes.size());

        Note theOnlyNote = notes.get(0);
        assertNotNull(theOnlyNote.id);
        assertEquals("hello", theOnlyNote.content);

        assertEquals(1, cassandraTemplate.count("note"));
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableCassandraRepositories
    public static class Config extends AbstractClusterConfiguration {
        private final static String KEYSPACE_NAME = "mydummykeyspace1";

        @Override
        protected List<CreateKeyspaceSpecification> getKeyspaceCreations() {
            CreateKeyspaceSpecification specification = CreateKeyspaceSpecification.createKeyspace(KEYSPACE_NAME)
                    .with(KeyspaceOption.DURABLE_WRITES, true)
                    .withSimpleReplication(1)/*
                    .ifNotExists()*/;
            return Arrays.asList(specification);
        }

        @Override
        protected List<DropKeyspaceSpecification> getKeyspaceDrops() {
            DropKeyspaceSpecification specification = DropKeyspaceSpecification.dropKeyspace(KEYSPACE_NAME);
            return Arrays.asList(specification);
        }
    }
}
