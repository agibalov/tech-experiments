package me.loki2302;

import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AppTest {
    private EmbeddedElasticSearch embeddedElasticSearch;
    private Client client;

    @Before
    public void startElasticSearch() {
        embeddedElasticSearch = new EmbeddedElasticSearch();
        embeddedElasticSearch.start();

        client = embeddedElasticSearch.client();
    }

    @After
    public void stopElasticSearch() {
        client = null;
        embeddedElasticSearch.stop();
        embeddedElasticSearch = null;
    }

    @Test
    public void thereAreNoIndicesByDefault() {
        assertThereAreNoIndices();
    }

    @Test
    public void canCreateAnIndexByAddingADocument() throws IOException, InterruptedException {
        IndexResponse indexResponse = client.prepareIndex("notes", "note", "1").setSource(
                XContentFactory.jsonBuilder()
                        .startObject()
                        .field("text", "hello")
                        .endObject())
                .execute()
                .actionGet();

        assertTrue(indexResponse.isCreated());
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

        DeleteResponse deleteResponse = client.prepareDelete("notes", "note", "1")
                .execute()
                .actionGet();
        assertTrue(deleteResponse.isFound());

        assertThereAreNoNotes();
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

    private void assertThereAreNoNotes() throws InterruptedException {
        assertThereAreNNotes(0);
    }

    private void assertThereAreNNotes(int expected) throws InterruptedException {
        Thread.sleep(1000);

        CountResponse countResponse = client.prepareCount("notes")
                .execute()
                .actionGet();
        assertEquals(expected, countResponse.getCount());
    }
}
