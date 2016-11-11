package me.loki2302;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AggregationTest {
    @Rule
    public final ElasticSearchRule elasticSearchRule = new ElasticSearchRule();

    private Client client;

    @Before
    public void init() throws IOException {
        client = elasticSearchRule.client;
        EmployeesDataset.populate(client);
    }

    @Test
    public void canGetAllInterests() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                .addAggregation(AggregationBuilders.terms("allInterests").field("interests"))
                .execute().actionGet();
        StringTerms allInterestsStringTerms = searchResponse.getAggregations().get("allInterests");
        List<Terms.Bucket> buckets = allInterestsStringTerms.getBuckets();
        assertEquals(3, buckets.size());
        assertEquals("music", buckets.get(0).getKey());
        assertEquals(2, buckets.get(0).getDocCount());
        assertEquals("forestry", buckets.get(1).getKey());
        assertEquals(1, buckets.get(1).getDocCount());
        assertEquals("sports", buckets.get(2).getKey());
        assertEquals(1, buckets.get(2).getDocCount());
    }

    @Test
    public void canGetAverageAge() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                .addAggregation(AggregationBuilders.avg("averageAge").field("age"))
                .execute().actionGet();
        Avg averageAge = searchResponse.getAggregations().get("averageAge");
        assertEquals(30.66, averageAge.getValue(), 0.01); // 30.6666666666666....
    }
}
