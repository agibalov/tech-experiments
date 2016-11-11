package me.loki2302;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MoreLikeThisQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;

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

        MoreLikeThisQueryBuilder moreLikeThisQueryBuilder = QueryBuilders.moreLikeThisQuery()
                .like(new MoreLikeThisQueryBuilder.Item("devices", "device", "1"))
                .minTermFreq(1)
                .minDocFreq(1);

        SearchResponse searchResponse = client.prepareSearch("devices")
                .setTypes("device")
                .setQuery(moreLikeThisQueryBuilder)
                .execute()
                .actionGet();

        assertEquals(1, searchResponse.getHits().totalHits());
        assertEquals("2", searchResponse.getHits().getAt(0).getId());
    }
}
