package me.loki2302;

import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppTest extends ElasticSearchTest {
    // TODO: this one fails sometimes. why?
    @Test
    public void thereAreNoIndicesByDefault() {
        assertThereAreNoIndices();
    }

    @Test
    public void canCreateAnIndexByAddingADocument() throws IOException {
        IndexResponse indexResponse = client.prepareIndex("notes", "note", "1").setSource(
                XContentFactory.jsonBuilder()
                        .startObject()
                        .field("text", "hello")
                        .endObject())
                .execute()
                .actionGet();
        assertTrue(indexResponse.isCreated());

        refreshIndices();

        assertThereAreNIndices(1);
        assertThereAreNNotes(1);
    }

    @Test
    public void canDeleteADocument() throws IOException, InterruptedException {
        client.prepareIndex("notes", "note", "1").setSource(
                XContentFactory.jsonBuilder()
                        .startObject()
                        .field("text", "hello")
                        .endObject())
                .execute()
                .actionGet();
        refreshIndices();

        DeleteResponse deleteResponse = client.prepareDelete("notes", "note", "1")
                .execute()
                .actionGet();
        assertTrue(deleteResponse.isFound());
        refreshIndices();

        assertThereAreNoNotes();
    }

    @Test
    public void canSearchDocuments() throws IOException, InterruptedException {
        client.prepareIndex("notes", "note", "1").setSource(
                XContentFactory.jsonBuilder()
                        .startObject().field("text", "remember the milk").endObject())
                .execute()
                .actionGet();
        client.prepareIndex("notes", "note", "2").setSource(
                XContentFactory.jsonBuilder()
                        .startObject().field("text", "get some vodka").endObject())
                .execute()
                .actionGet();

        refreshIndices();

        SearchResponse searchResponse = client.prepareSearch("notes")
                .setTypes("note")
                .setSearchType(SearchType.QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.termQuery("text", "milk"))
                .execute()
                .actionGet();

        SearchHits searchHits = searchResponse.getHits();
        assertEquals(1, searchHits.totalHits());
        SearchHit[] searchHitsArray = searchHits.getHits();
        SearchHit firstSearchHit = searchHitsArray[0];
        assertEquals("1", firstSearchHit.id());
        assertEquals("remember the milk", firstSearchHit.sourceAsMap().get("text"));
    }

    private void assertThereAreNoIndices() {
        assertThereAreNIndices(0);
    }

    private void assertThereAreNIndices(int expected) {
        IndicesStatsResponse indicesStatsResponse = client.admin().indices().prepareStats()
                .execute()
                .actionGet();
        assertEquals(expected, indicesStatsResponse.getIndices().size());
    }

    private void assertThereAreNoNotes() {
        assertThereAreNNotes(0);
    }

    private void assertThereAreNNotes(int expected) {
        CountResponse countResponse = client.prepareCount("notes")
                .execute()
                .actionGet();
        assertEquals(expected, countResponse.getCount());
    }

    private void refreshIndices() {
        client.admin().indices().prepareRefresh().execute().actionGet();
    }
}
