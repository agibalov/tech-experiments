package me.loki2302;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.highlight.HighlightField;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HighlightingTest {
    @Rule
    public final ElasticSearchRule elasticSearchRule = new ElasticSearchRule();

    private Client client;

    @Before
    public void init() throws IOException {
        client = elasticSearchRule.client;
    }

    @Test
    public void canUseHighlighting() throws IOException {
        client.prepareIndex("notes", "note", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("text", "Once upon a time there was Java")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("notes", "note", "2")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("text", "Sponge Bob rocks!")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        SearchResponse searchResponse = client.prepareSearch("notes")
                .setTypes("note")
                .setQuery(QueryBuilders.matchQuery("text", "bob"))
                .addHighlightedField("text")
                .execute().actionGet();

        SearchHits searchHits = searchResponse.getHits();
        assertEquals(1, searchHits.getTotalHits());

        SearchHit searchHit = searchHits.getAt(0);
        Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
        assertEquals(1, highlightFields.size());
        assertTrue(highlightFields.containsKey("text"));

        HighlightField textHighlightField = highlightFields.get("text");
        assertEquals("Sponge <em>Bob</em> rocks!", textHighlightField.getFragments()[0].string());
    }
}
