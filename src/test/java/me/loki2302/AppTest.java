package me.loki2302;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
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
    public void dummy() throws IOException, InterruptedException {
        IndexResponse indexResponse = client.prepareIndex("notes", "note", "1").setSource(
                XContentFactory.jsonBuilder()
                        .startObject()
                        .field("text", "hello")
                        .endObject())
                .execute()
                .actionGet();
        assertTrue(indexResponse.isCreated());

        GetResponse getResponse = client.prepareGet("notes", "note", "1")
                .execute()
                .actionGet();
        assertEquals("hello", getResponse.getSource().get("text"));

        Thread.sleep(1000);

        CountResponse countResponse = client.prepareCount("notes")
                .execute()
                .actionGet();
        assertEquals(1, countResponse.getCount());

        DeleteResponse deleteResponse = client.prepareDelete("notes", "note", "1")
                .execute()
                .actionGet();
        assertTrue(deleteResponse.isFound());

        Thread.sleep(1000);

        CountResponse afterDeleteCountResponse = client.prepareCount("notes")
                .setQuery(termQuery("_type", "note"))
                .execute()
                .actionGet();
        assertEquals(0, afterDeleteCountResponse.getCount());
    }
}
