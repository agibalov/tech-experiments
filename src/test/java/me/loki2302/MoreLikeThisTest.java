package me.loki2302;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MoreLikeThisTest extends ElasticSearchTest {
    @Test
    public void canUseMoreLikeThis() throws IOException {
        client.prepareIndex("devices", "device", "1")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "apple iphone 6")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("devices", "device", "2")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "apple iphone 5")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        client.prepareIndex("devices", "device", "3")
                .setSource(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("name", "google nexus 7")
                        .endObject())
                .setRefresh(true)
                .execute().actionGet();

        SearchResponse searchResponse = client.prepareMoreLikeThis("devices", "device", "1")
                .setField("name")
                .setMinTermFreq(1)
                .setMinDocFreq(1)
                .execute().actionGet();

        assertEquals(1, searchResponse.getHits().totalHits());
        assertEquals("2", searchResponse.getHits().getAt(0).getId());
    }
}
