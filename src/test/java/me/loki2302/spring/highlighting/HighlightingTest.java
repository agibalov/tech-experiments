package me.loki2302.spring.highlighting;

import me.loki2302.spring.ElasticsearchIntegrationTestUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.highlight.HighlightField;
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
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
public class HighlightingTest {
    @Autowired
    private ElasticsearchIntegrationTestUtils elasticsearchIntegrationTestUtils;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Before
    public void init() throws IOException {
        elasticsearchIntegrationTestUtils.reset();
    }

    @Test
    public void canUseHighlighting() throws IOException {
        noteRepository.save(makeNote("Once upon a time there was Java"));
        noteRepository.save(makeNote("Sponge Bob rocks!"));

        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("text", "bob"))
                .withHighlightFields(new HighlightBuilder.Field("text"))
                .build();

        SearchResponse searchResponse = elasticsearchOperations.query(nativeSearchQuery, response -> response);

        SearchHits searchHits = searchResponse.getHits();
        assertEquals(1, searchHits.getTotalHits());

        SearchHit searchHit = searchHits.getAt(0);
        Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
        assertEquals(1, highlightFields.size());
        assertTrue(highlightFields.containsKey("text"));

        HighlightField textHighlightField = highlightFields.get("text");
        assertEquals("Sponge <em>Bob</em> rocks!", textHighlightField.getFragments()[0].string());
    }

    private static Note makeNote(String text) {
        Note note = new Note();
        note.text = text;
        return note;
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
