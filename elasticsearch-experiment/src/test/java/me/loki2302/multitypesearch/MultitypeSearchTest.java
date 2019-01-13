package me.loki2302.multitypesearch;

import me.loki2302.ElasticsearchIntegrationTestUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class MultitypeSearchTest {
    @Autowired
    private ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private EventRepository eventRepository;

    @Before
    public void init() throws IOException {
        elasticsearchIntegrationTestUtils.reset();

        noteRepository.save(note("1", "get some vodka"));
        noteRepository.save(note("2", "get some beer"));
        eventRepository.save(event("1", "the vodka party!"));
        eventRepository.save(event("2", "the beer party!"));
        eventRepository.save(event("3", "the sponge bob party!"));
    }

    private static Note note(String id, String text) {
        Note note = new Note();
        note.id = id;
        note.text = text;
        return note;
    }

    private static Event event(String id, String text) {
        Event event = new Event();
        event.id = id;
        event.text = text;
        return event;
    }

    @Test
    public void canFindEverything() {
        SearchResponse searchResponse = elasticsearchOperations.query(new NativeSearchQueryBuilder()
                .withIndices("notebook")
                .build(), response -> response);

        assertEquals(5, searchResponse.getHits().getTotalHits());
    }

    @Test
    public void canFindAllNotesAndEvents() {
        SearchResponse searchResponse = elasticsearchOperations.query(new NativeSearchQueryBuilder()
                .withIndices("notebook")
                .withTypes("note", "event")
                .build(), response -> response);

        assertEquals(5, searchResponse.getHits().getTotalHits());
    }

    @Test
    public void canFindEverythingAboutVodka() {
        SearchResponse searchResponse = elasticsearchOperations.query(new NativeSearchQueryBuilder()
                .withIndices("notebook")
                .withQuery(QueryBuilders.matchQuery("text", "vodka"))
                .build(), response -> response);

        assertEquals(2, searchResponse.getHits().getTotalHits());
    }

    @Test
    public void canFindVodkaNotesAndEvents() {
        SearchResponse searchResponse = elasticsearchOperations.query(new NativeSearchQueryBuilder()
                .withIndices("notebook")
                .withTypes("note", "event")
                .withQuery(QueryBuilders.matchQuery("text", "vodka"))
                .build(), response -> response);

        assertEquals(2, searchResponse.getHits().getTotalHits());
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan
    public static class Config {
        @Bean
        public ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils() {
            return new ElasticsearchIntegrationTestUtils();
        }
    }
}
