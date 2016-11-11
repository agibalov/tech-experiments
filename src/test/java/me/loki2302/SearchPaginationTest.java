package me.loki2302;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SearchPaginationTest {
    @Rule
    public final ElasticSearchRule elasticSearchRule = new ElasticSearchRule();

    private Client client;

    @Before
    public void init() throws IOException {
        client = elasticSearchRule.client;
        EmployeesDataset.populate(client);
    }

    @Test
    public void canGetYoungestEmployee() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                .setQuery(QueryBuilders.matchAllQuery())
                .addSort(SortBuilders.fieldSort("age"))
                .setSize(1)
                .setFrom(0)
                .execute().actionGet();

        SearchHit searchHit = searchResponse.getHits().getAt(0);
        assertEquals("1", searchHit.getId());

        Map<String, Object> source = searchHit.getSource();
        assertEquals(25, (int)(Integer)source.get("age"));
    }
}
