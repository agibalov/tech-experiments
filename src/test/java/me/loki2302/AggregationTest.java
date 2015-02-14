package me.loki2302;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.metrics.avg.Avg;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AggregationTest extends ElasticSearchTest {
    @Before
    public void populate() throws IOException {
        EmployeesDataset.populate(client);
    }

    @Test
    public void canGetAllInterests() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                .addAggregation(AggregationBuilders.terms("allInterests").field("interests"))
                .execute().actionGet();
        StringTerms allInterestsStringTerms = searchResponse.getAggregations().get("allInterests");
        assertEquals(3, allInterestsStringTerms.getBuckets().size());
    }

    @Test
    public void canGetAverageAge() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                .addAggregation(AggregationBuilders.avg("averageAge").field("age"))
                .execute().actionGet();
        Avg averageAge = searchResponse.getAggregations().get("averageAge");
        assertEquals(30.66, averageAge.getValue(), 0.01); // 30.6666666666666....
    }

    // +TODO: get all tags
    // TODO: get all tags with person count per each tag
    // TODO: get min age and the related person
    // +TODO: get avg age
}
