package me.loki2302;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SearchTest extends ElasticSearchTest {
    // TODO: split into individual tests

    @Before
    public void populate() throws IOException {
        EmployeesDataset.populate(client);
    }

    @Test
    public void canSearchEmployees() throws IOException, InterruptedException {
        SearchResponse searchByLastNameTermQueryResponse = client.prepareSearch("megacorp").setTypes("employee")
                // TODO: quite weird. When I save a value like "Hello-There",
                // TODO: the index is built for "hello" and "there",
                // TODO: so in case of "Smith", I only can search by "smith" :-/
                .setQuery(QueryBuilders.termQuery("lastName", "smith"))
                .execute().actionGet();
        assertEquals(2, searchByLastNameTermQueryResponse.getHits().totalHits());

        SearchResponse searchByLastNameMatchQueryResponse = client.prepareSearch("megacorp").setTypes("employee")
                // as opposed to termQuery, this will first convert "Smith" to "smith"
                .setQuery(QueryBuilders.matchQuery("lastName", "Smith"))
                .execute().actionGet();
        assertEquals(2, searchByLastNameMatchQueryResponse.getHits().totalHits());

        SearchResponse searchByAgeTermQueryResponse = client.prepareSearch("megacorp").setTypes("employee")
                .setQuery(QueryBuilders.termQuery("age", 35))
                .execute().actionGet();
        assertEquals(1, searchByAgeTermQueryResponse.getHits().totalHits());

        SearchResponse searchByAgeRangeQueryResponse = client.prepareSearch("megacorp").setTypes("employee")
                .setQuery(QueryBuilders.rangeQuery("age").from(32))
                .execute().actionGet();
        assertEquals(2, searchByAgeRangeQueryResponse.getHits().totalHits());

        SearchResponse searchByAboutMatchQueryResponse = client.prepareSearch("megacorp").setTypes("employee")
                // this will look for "rock" and "climbing", not for "rock climbing"
                .setQuery(QueryBuilders.matchQuery("about", "rock climbing"))
                .execute().actionGet();
        assertEquals(2, searchByAboutMatchQueryResponse.getHits().totalHits());

        SearchResponse searchByAboutMatchPhraseQueryResponse = client.prepareSearch("megacorp").setTypes("employee")
                // as opposed to matchQuery, this will look for "rock climbing"
                .setQuery(QueryBuilders.matchPhraseQuery("about", "rock climbing"))
                .execute().actionGet();
        assertEquals(1, searchByAboutMatchPhraseQueryResponse.getHits().totalHits());

        SearchResponse searchByOneInterestTermQueryResponse = client.prepareSearch("megacorp").setTypes("employee")
                .setQuery(QueryBuilders.termQuery("interests", "music"))
                .execute().actionGet();
        assertEquals(2, searchByOneInterestTermQueryResponse.getHits().totalHits());

        SearchResponse searchByTwoInterestsTermQueryResponse = client.prepareSearch("megacorp").setTypes("employee")
                // at least one of "music" and "sports", not necessarily both
                .setQuery(QueryBuilders.termsQuery("interests", "music", "sports"))
                .execute().actionGet();
        assertEquals(2, searchByTwoInterestsTermQueryResponse.getHits().totalHits());

        SearchResponse allInterestsAggregationResponse = client.prepareSearch("megacorp").setTypes("employee")
                .addAggregation(AggregationBuilders.terms("allInterests").field("interests"))
                .execute().actionGet();
        StringTerms allInterestsStringTerms = allInterestsAggregationResponse.getAggregations().get("allInterests");
        assertEquals(3, allInterestsStringTerms.getBuckets().size());
    }
}
