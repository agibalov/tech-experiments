package me.loki2302;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
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
        SearchResponse allInterestsAggregationResponse = client.prepareSearch("megacorp").setTypes("employee")
                .addAggregation(AggregationBuilders.terms("allInterests").field("interests"))
                .execute().actionGet();
        StringTerms allInterestsStringTerms = allInterestsAggregationResponse.getAggregations().get("allInterests");
        assertEquals(3, allInterestsStringTerms.getBuckets().size());
    }

    // +TODO: get all tags
    // TODO: get all tags with person count per each tag
    // TODO: get min age and the related person
    // TODO: get avg age
}
