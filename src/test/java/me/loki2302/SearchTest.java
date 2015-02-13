package me.loki2302;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class SearchTest extends ElasticSearchTest {
    // TODO: extend the tests, add more specific checks

    @Before
    public void populate() throws IOException {
        EmployeesDataset.populate(client);
    }

    @Test
    public void canSearchEmployeesWithLastNameTermQuery() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                // TODO: quite weird. When I save a value like "Hello-There",
                // TODO: the index is built for "hello" and "there",
                // TODO: so in case of "Smith", I only can search by "smith" :-/
                .setQuery(QueryBuilders.termQuery("lastName", "smith"))
                .execute().actionGet();
        assertEquals(2, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithLastNameMatchQuery() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                // as opposed to termQuery, this will first convert "Smith" to "smith"
                .setQuery(QueryBuilders.matchQuery("lastName", "Smith"))
                .execute().actionGet();
        assertEquals(2, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithAgeTermQuery() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                .setQuery(QueryBuilders.termQuery("age", 35))
                .execute().actionGet();
        assertEquals(1, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithAgeRangeQuery() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                .setQuery(QueryBuilders.rangeQuery("age").from(32))
                .execute().actionGet();
        assertEquals(2, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithAboutMatchQuery() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                // this will look for "rock" and "climbing", not for "rock climbing"
                .setQuery(QueryBuilders.matchQuery("about", "rock climbing"))
                .execute().actionGet();
        assertEquals(2, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithAboutMatchPhraseQuery() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                // as opposed to matchQuery, this will look for "rock climbing"
                .setQuery(QueryBuilders.matchPhraseQuery("about", "rock climbing"))
                .execute().actionGet();
        assertEquals(1, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithOneInterestTermQuery() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                .setQuery(QueryBuilders.termQuery("interests", "music"))
                .execute().actionGet();
        assertEquals(2, searchResponse.getHits().totalHits());
    }

    @Test
    public void canSearchEmployeesWithTwoInterestTermsQuery() {
        SearchResponse searchResponse = client.prepareSearch("megacorp").setTypes("employee")
                // at least one of "music" and "sports", not necessarily both
                .setQuery(QueryBuilders.termsQuery("interests", "music", "sports"))
                .execute().actionGet();
        assertEquals(2, searchResponse.getHits().totalHits());
    }
}
