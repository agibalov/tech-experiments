package me.loki2302;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MultitypeSearchTest extends ElasticSearchTest {
    @Before
    public void populate() throws IOException {
        NotesAndEventsDataset.populate(client);
    }

    @Test
    public void canFindEverything() {
        SearchResponse searchResponse = client.prepareSearch("notebook")
                .execute().actionGet();
        assertEquals(5, searchResponse.getHits().getTotalHits());
    }

    @Test
    public void canFindAllNotesAndEvents() {
        SearchResponse searchResponse = client.prepareSearch("notebook")
                .setTypes("note", "event")
                .execute().actionGet();
        assertEquals(5, searchResponse.getHits().getTotalHits());
    }

    @Test
    public void canFindEverythingAboutVodka() {
        SearchResponse searchResponse = client.prepareSearch("notebook")
                .setQuery(QueryBuilders.matchQuery("text", "vodka"))
                .execute().actionGet();
        assertEquals(2, searchResponse.getHits().getTotalHits());
    }

    @Test
    public void canFindVodkaNotesAndEvents() {
        SearchResponse searchResponse = client.prepareSearch("notebook")
                .setTypes("note", "event")
                .setQuery(QueryBuilders.matchQuery("text", "vodka"))
                .execute().actionGet();
        assertEquals(2, searchResponse.getHits().getTotalHits());
    }

    public static class NotesAndEventsDataset {
        public static void populate(Client client) throws IOException {
            makeNote(client, "1", "get some vodka");
            makeNote(client, "2", "get some beer");
            makeEvent(client, "1", "the vodka party!");
            makeEvent(client, "2", "the beer party!");
            makeEvent(client, "3", "the sponge bob party!");
        }

        private static void makeNote(Client client, String id, String text) throws IOException {
            client.prepareIndex("notebook", "note", id)
                    .setSource(XContentFactory.jsonBuilder()
                            .startObject()
                            .field("text", text)
                            .endObject())
                    .setRefresh(true)
                    .execute().actionGet();
        }

        private static void makeEvent(Client client, String id, String text) throws IOException {
            client.prepareIndex("notebook", "event", id)
                    .setSource(XContentFactory.jsonBuilder()
                            .startObject()
                            .field("text", text)
                            .endObject())
                    .setRefresh(true)
                    .execute().actionGet();
        }
    }
}
